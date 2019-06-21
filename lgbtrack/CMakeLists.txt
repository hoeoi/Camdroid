cmake_minimum_required (VERSION 2.8)

set(distribution_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
set(mainPath ${CMAKE_SOURCE_DIR}/src/main/)
set(sourcePath ${CMAKE_SOURCE_DIR}/src/main/lgbTrack)

aux_source_directory (sourcePath SRC)

add_subdirectory (${sourcePath}/KCFTracker)

add_subdirectory (${sourcePath}/CMT)

include_directories (${sourcePath}/KCFTracker
                    ${sourcePath}/CMT
                    ${sourcePath}/include)

link_directories (  ${sourcePath}/lib/opencv/${ANDROID_ABI}
                    ${sourcePath}/lib/3rdparty/${ANDROID_ABI})



add_library (lgbTrack
                SHARED
                ${sourcePath}/LGBTrack.cpp
                ${mainPath}/jni/lgbTrackJni.cpp)


target_link_libraries (	lgbTrack 
						kcf
						cmt
                        opencv_shape
                        opencv_ml
                        opencv_dnn
                        opencv_objdetect
                        opencv_superres
                        opencv_stitching
                        opencv_videostab
                        opencv_calib3d
                        opencv_features2d
                        opencv_highgui
                        opencv_videoio
                        opencv_imgcodecs
                        opencv_video
                        opencv_photo
                        opencv_imgproc
                        opencv_flann
                        opencv_core
						cpufeatures
                        IlmImf
                        libjasper
                        libjpeg
                        libpng
                        libprotobuf
                        libtiff
                        libwebp
                        tbb
                        tegra_hal
                        log
                        z
                        )




#SET(CMAKE_BUILE_TYPE "RELEASE")
#SET(CMAKE_CXX_FLAGS_RELEASE "$ENV{CXXFLAGS} -O3 -Wall")
SET(CMAKE_BUILE_TYPE "DEBUG")
SET(CMAKE_CXX_FLAGS_DEBUG "-g -Wall")

