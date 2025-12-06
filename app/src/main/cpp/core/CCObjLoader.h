#pragma once
#ifndef VR720_COBJ_LOADER_H
#define VR720_COBJ_LOADER_H
#include "CCMeshLoader.h"

//Obj model loader
class CCObjLoader : public CCMeshLoader
{
	const char* LOG_TAG = "CCObjLoader";

    /**
	 * Load Mesh from file
	 * @param path File path
	 * @param mesh The Mesh to be loaded
	 * @return Returns whether successful
	 */
	bool Load(const char* path, CCMesh* mesh) override;
	/**
	 * Load Mesh from memory data
	 * @param buffer mesh data memory
	 * @param bufferSize mesh data size
	 * @param mesh The Mesh to be loaded
	 * @return Returns whether successful
	 */
	bool Load(BYTE * buffer, size_t bufferSize, CCMesh *mesh) override;
};

#endif
