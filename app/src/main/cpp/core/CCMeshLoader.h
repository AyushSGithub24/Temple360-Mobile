#pragma once
#ifndef VR720_CCMESHLOADER_H
#define VR720_CCMESHLOADER_H
#include "stdafx.h"

//Mesh Type
enum class CCMeshType {
	MeshTypeObj,
	MeshTypeFbx,
};

class CCMesh;
//Mesh Loader
class CCMeshLoader
{
public:
	/**
	 * Get the Mesh loader for the specified type
	 * @param type Type, CCMeshType
	 * @return Returns the loader
	 */
	static CCMeshLoader* GetMeshLoaderByType(CCMeshType type);
	/**
	 * Get the Mesh loader of the specified type by file path
	 * @param path Mesh file path
	 * @return Returns the loader
	 */
	static CCMeshLoader* GetMeshLoaderByFilePath(const char* path);
	/**
	 * Global initialization
	 */
	static void Init();
	/**
	 * Globally release resources
	 */
	static void Destroy();

	/**
	 * Load Mesh from file
	 * @param path File path
	 * @param mesh The Mesh to be loaded
	 * @return Returns whether successful
	 */
	virtual bool Load(const char * path, CCMesh *mesh);
	/**
	 * Load Mesh from memory data
	 * @param buffer mesh data memory
	 * @param bufferSize mesh data size
	 * @param mesh The Mesh to be loaded
	 * @return Returns whether successful
	 */
	virtual bool Load(BYTE * buffer, size_t bufferSize, CCMesh *mesh);
	/**
	 * Get the error that occurred in the last load
	 * @return Loading error
	 */
	virtual const char* GetLastError();

protected:
	/**
	 * Set loading error
	 * @param err Loading error
	 */
	void SetLastError(const char* err);
private:
	std::string lastErr;
};

#endif
