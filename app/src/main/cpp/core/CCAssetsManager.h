#ifndef VR720_CCASSETSMANAGER_H
#define VR720_CCASSETSMANAGER_H

#include "stdafx.h"

class CCMesh;
class CCTexture;
/**
 * Asset management
 */
class CCAssetsManager {

public:
    /**
     * Get resource path
     * @param typeName Type folder
     * @param name Resource name
     * @return
     */
    static std::string GetResourcePath(const char* typeName, const char* name);
    /**
     * Get custom folder path
     * @param dirName Folder
     * @param name Resource name
     * @return
     */
    static std::string GetDirResourcePath(const char* dirName, const char* name);

    /**
     * Load resources into memory
     * @param path Full path of the resource
     * @param bufferLength Used to store the buffer size
     * @return Returns the data buffer
     */
    static BYTE* LoadResource(const char* path, size_t *bufferLength);

    /**
     * Load string resources into memory
     * @param path Full path of the resource
     * @param bufferLength Used to store the buffer size
     * @return Returns the data buffer
     */
    static std::string LoadStringResource(const char* path);

    /**
     * Load texture from file
     * @param path Texture path
     * @return Returns the texture, if loading fails, returns nullptr
     */
    static CCTexture* LoadTexture(const char* path);

    /**
     * Load mesh from file
     * @param path Mesh path
     * @return Returns the mesh, if loading fails, returns nullptr
     */
    static CCMesh* LoadMesh(const char* path);

    /**
     * Resource management is initialized from JNI
     * @param env JNIEnv
     * @param assetManager getAssets
     */
    static void Android_InitFromJni(JNIEnv* env, jobject assetManager);
    /**
     * Load Android assets folder resources. The returned buffer needs to be free after use
     * @param path Resource path
     * @param buffer Used to store the buffer address
     * @param bufferLength Used to store the buffer size
     * @return Returns whether successful
     */
    static bool Android_LoadAsset(const char* path, BYTE **buffer, size_t *bufferLength);

};


#endif //VR720_CCASSETSMANAGER_H
