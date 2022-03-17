cmake_minimum_required(VERSION 3.4.1)
add_library(jackpal-androidterm5 SHARED
 src/main/cpp/common.cpp
 src/main/cpp/fileCompat.cpp
 src/main/cpp/termExec.cpp
)

find_library(log log)
target_link_libraries(jackpal-androidterm5 log)