#pragma once
#ifndef VR720_CCMATERIAL_H
#define VR720_CCMATERIAL_H
#include "stdafx.h"
#include "CCTexture.h"
#include <vector>

//Simple material
class CCMaterial
{
public:
	CCMaterial();
	CCMaterial(CCSmartPtr<CCTexture> & diffuse);
	~CCMaterial();

	//Diffuse map
	CCSmartPtr<CCTexture> diffuse = nullptr;

	//Texture tiling
	glm::vec2 tilling = glm::vec2(1.0f, 1.0f);
	//Texture offset
	glm::vec2 offest = glm::vec2(0.0f, 0.0f);

	/**
	 * Use current material
	 */
	void Use() const;
};

#endif

