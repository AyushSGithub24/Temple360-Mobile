#pragma once
#ifndef VR720_CCFILEMANAGER_H
#define VR720_CCFILEMANAGER_H
#include "stdafx.h"
#include "../imageloaders/CImageLoader.h"
#include "CCErrors.h"
#include <string>

//Load texture callback
typedef void (*CCFileManagerOnCloseCallback)(void* data);

#define CC_FILE_TYPE_JPG 3
#define CC_FILE_TYPE_PNG 4
#define CC_FILE_TYPE_BMP 5

#define CC_FILE_TYPE_IMG_MAX 10
#define CC_FILE_TYPE_MP4 11
#define CC_FILE_TYPE_AVI 12
#define CC_FILE_TYPE_WMV 13
#define CC_FILE_TYPE_RMVB 14
#define CC_FILE_TYPE_MPG 15
#define CC_FILE_TYPE_3GP 16
#define CC_FILE_TYPE_MOV 17
#define CC_FILE_TYPE_MKV 18
#define CC_FILE_TYPE_FLV 19

#define CC_FILE_TYPE_VIDEO_MAX 20

//Determine if the file type is an image
#define CC_IS_FILE_TYPE_IMAGE(x) x < CC_FILE_TYPE_IMG_MAX
//Determine if the file type is a video
#define CC_IS_FILE_TYPE_VIDEO(x) (x > CC_FILE_TYPE_IMG_MAX && x < CC_FILE_TYPE_VIDEO_MAX)

/**
 * File open management
 */
class COpenGLRenderer;
class CCFileManager
{
public:

    CCFileManager(COpenGLRenderer *render);

	/**
	 * Open file
	 * @param path File path
	 * @return Returns whether successful
	 */
    bool OpenFile(const char* path);
    /**
     * Close file
     */
    void CloseFile();
    /**
     * Get the path of the currently open file
     * @return Returns the path
     */
    std::string GetCurrentFileName() const ;

    /**
     * Get the type of the currently open file
     * @return Returns the file type
     */
    int CheckCurrentFileType() const;

    CImageLoader* CurrentFileLoader = nullptr;
    ImageType CurrenImageType = ImageType::Unknow;
    std::string CurrenImagePath;

    /**
     * Set file close callback
     * @param c callback
     * @param data Callback custom data
     */
    void SetOnCloseCallback(CCFileManagerOnCloseCallback c, void* data) {
        onCloseCallback = c;
        onCloseCallbackData = data;
    }

    /**
     * Get last error
     * @return Last error
     */
    const int GetLastError() { return lastErr; }

    const char* LOG_TAG = "CCFileManager";
private:
    Logger* logger = nullptr;

    int lastErr;
    COpenGLRenderer* Render = nullptr;

    CCFileManagerOnCloseCallback onCloseCallback = nullptr;
    void*onCloseCallbackData = nullptr;

};

#endif


