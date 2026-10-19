#ifndef STUDYFLOW_SLAB_ALLOCATOR_H
#define STUDYFLOW_SLAB_ALLOCATOR_H

#include <cstddef>
#include <cstdint>
#include <string>
#include <vector>
#include <mutex>

namespace studyflow {

struct MemoryPoolStats {
    size_t pageSizeBytes;
    size_t totalAllocatedBytes;
    size_t activePhysicalBytes;
    size_t dirtyPagesCount;
    size_t madviseReleasesCount;
    size_t pageFaultsPrevented;
};

class DenseSlabAllocator {
public:
    static DenseSlabAllocator& getInstance();

    // Dynamically queries sysconf(_SC_PAGESIZE) for 16KB/4KB alignment
    size_t getSystemPageSize() const;

    // Allocates memory using dense-first slab strategy
    void* allocate(size_t bytes);

    // Deallocates memory; uses madvise(..., MADV_DONTNEED) for blocks > 64KB
    void deallocate(void* ptr, size_t bytes);

    // Get real-time engine memory diagnostics
    MemoryPoolStats getStats();

    std::string getDiagnosticsString();

private:
    DenseSlabAllocator();
    ~DenseSlabAllocator();

    size_t mPageSize;
    size_t mTotalAllocated;
    size_t mActivePhysical;
    size_t mDirtyPages;
    size_t mMadviseCount;
    size_t mPageFaultsPrevented;
    std::mutex mMutex;

    // Internal slab tracking
    struct SlabChunk {
        void* address;
        size_t size;
        bool isFree;
        size_t pageOffset;
    };
    std::vector<SlabChunk> mSlabs;
};

} // namespace studyflow

#endif // STUDYFLOW_SLAB_ALLOCATOR_H
