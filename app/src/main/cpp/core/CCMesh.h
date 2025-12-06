#pragma once
#ifndef VR720_CCMAESH_H
#define VR720_CCMAESH_H
#include "stdafx.h"
#include <vector>

//Face information
class CCFace
{
public:
	CCFace(unsigned int vertex_index, unsigned int normal_index = 0, unsigned int texcoord_index = -1);

	unsigned int vertex_index;
	unsigned int normal_index;
	unsigned int texcoord_index;
};

//Simple mesh class
class CCMesh
{
public:
	CCMesh();
	~CCMesh();

	/**
	 * Generate mesh buffer
	 */
	void GenerateBuffer();
	/**
	 * Re-buffer data
	 */
	void ReBufferData();
	/**
	 * Release mesh buffer
	 */
	void ReleaseBuffer();
	/**
	 * Render Mesh
	 */
	void RenderMesh() const;
	/**
	 * Load from obj file
	 * @param path obj file path
	 */
	void LoadFromObj(const char* path);
	/**
	 * Clear loaded data and buffers
	 */
	void UnLoad();

	GLuint MeshVBO = 0;
	GLenum DrawType = GL_STATIC_DRAW;

	//Vertex data
	std::vector<glm::vec3> positions;
	//Normal vector data
	std::vector<glm::vec3> normals;
	//Index data
	std::vector<CCFace> indices;
	//uv data
	std::vector<glm::vec2> texCoords;
};

#endif

