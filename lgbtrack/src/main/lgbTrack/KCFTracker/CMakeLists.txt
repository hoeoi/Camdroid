cmake_minimum_required (VERSION 2.8)

include_directories (${CMAKE_SOURCE_DIR}/src/main/lgbTrack/include)
include_directories (${CMAKE_SOURCE_DIR}/src/main/lgbTrack/)
aux_source_directory (. SRC)

add_library (kcf ${SRC})