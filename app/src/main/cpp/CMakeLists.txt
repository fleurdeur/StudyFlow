cmake_minimum_required(VERSION 3.22.1)

project("studyflow_native")

# C++20 Standard Mandate
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 16 KB Memory Page Size NDK r27+ Linker Flag
# Mandated by modern Android hardware and Google Play compliance
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

# AutoFDO & Profile-Guided Optimization (PGO) readiness flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fomit-frame-pointer -ffast-math -fvisibility=hidden")

add_library(
    studyflow_native
    SHARED
    jni_bridge.cpp
    pdf_extract.cpp
    categorizer.cpp
    reviewer.cpp
    slab_allocator.cpp
    vector_search.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    studyflow_native
    ${log-lib}
)
