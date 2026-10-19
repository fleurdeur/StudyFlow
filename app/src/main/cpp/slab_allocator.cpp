#include "slab_allocator.h"
#include <unistd.h>
#include <sys/mman.h>
#include <sstream>
#include <cstring>
#include <algorithm>

namespace studyflow {

DenseSlabAllocator& DenseSlabAllocator::getInstance() {
    static DenseSlabAllocator instance;
    return instance;
}

DenseSlabAllocator::DenseSlabAllocator()
    : mTotalAllocated(0),
      mActivePhysical(0),
      mDirtyPages(0),
      mMadviseCount(0),
      mPageFaultsPrevented(0) {
    // Dynamic 16 KB Page Size detection via sysconf(_SC_PAGESIZE)
    long sysPage = sysconf(_SC_PAGESIZE);
    if (sysPage > 0) {
        mPageSize = static_cast<size_t>(sysPage);
    } else {
        mPageSize = 16384; // Default to 16 KB modern Android hardware page size
    }
}

DenseSlabAllocator::~DenseSlabAllocator() {
    std::lock_guard<std::mutex> lock(mMutex);
    for (auto& slab : mSlabs) {
        if (slab.address) {
            munmap(slab.address, slab.size);
            slab.address = nullptr;
        }
    }
    mSlabs.clear();
}

size_t DenseSlabAllocator::getSystemPageSize() const {
    return mPageSize;
}

void* DenseSlabAllocator::allocate(size_t bytes) {
    std::lock_guard<std::mutex> lock(mMutex);

    // Round up allocation to a multiple of 16 bytes for alignment
    size_t alignedBytes = (bytes + 15) & ~15;

    // Dense-First search: Find existing free chunk in partially dirty 16 KB pages
    for (auto& slab : mSlabs) {
        if (slab.isFree && slab.size >= alignedBytes) {
            slab.isFree = false;
            mPageFaultsPrevented++;
            mActivePhysical += slab.size;
            return slab.address;
        }
    }

    // Allocate new page-aligned block (multiple of mPageSize: 16 KB)
    size_t pagesNeeded = (alignedBytes + mPageSize - 1) / mPageSize;
    size_t totalBlockSize = pagesNeeded * mPageSize;

    void* ptr = mmap(
        nullptr,
        totalBlockSize,
        PROT_READ | PROT_WRITE,
        MAP_PRIVATE | MAP_ANONYMOUS,
        -1,
        0
    );

    if (ptr == MAP_FAILED) {
        return nullptr;
    }

    SlabChunk chunk;
    chunk.address = ptr;
    chunk.size = totalBlockSize;
    chunk.isFree = false;
    chunk.pageOffset = 0;
    mSlabs.push_back(chunk);

    mTotalAllocated += totalBlockSize;
    mActivePhysical += totalBlockSize;
    mDirtyPages += pagesNeeded;

    return ptr;
}

void DenseSlabAllocator::deallocate(void* ptr, size_t bytes) {
    if (!ptr) return;
    std::lock_guard<std::mutex> lock(mMutex);

    for (auto& slab : mSlabs) {
        if (slab.address == ptr) {
            slab.isFree = true;
            mActivePhysical = (mActivePhysical >= slab.size) ? (mActivePhysical - slab.size) : 0;

            // Immediate Physical RAM Release: Call madvise(MADV_DONTNEED) for buffers > 64 KB
            if (slab.size > (64 * 1024)) {
                madvise(slab.address, slab.size, MADV_DONTNEED);
                mMadviseCount++;
            }
            return;
        }
    }
}

MemoryPoolStats DenseSlabAllocator::getStats() {
    std::lock_guard<std::mutex> lock(mMutex);
    MemoryPoolStats s;
    s.pageSizeBytes = mPageSize;
    s.totalAllocatedBytes = mTotalAllocated;
    s.activePhysicalBytes = mActivePhysical;
    s.dirtyPagesCount = mDirtyPages;
    s.madviseReleasesCount = mMadviseCount;
    s.pageFaultsPrevented = mPageFaultsPrevented;
    return s;
}

std::string DenseSlabAllocator::getDiagnosticsString() {
    auto s = getStats();
    std::ostringstream oss;
    oss << "16KB Page Engine (PageSize=" << (s.pageSizeBytes / 1024) << "KB"
        << ", Slabs=" << mSlabs.size()
        << ", madvise=" << s.madviseReleasesCount
        << ", FastReuse=" << s.pageFaultsPrevented << ")";
    return oss.str();
}

} // namespace studyflow
