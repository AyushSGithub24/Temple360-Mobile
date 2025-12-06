#pragma once
#include "stdafx.h"
#include "CCamera.h"

//Camera panorama mode
enum class CCPanoramaCameraMode {
	CenterRoate,
	OutRoataround,
	Static,
	OrthoZoom,
};
//Camera movement options
enum class CCameraMovement {
	FORWARD,
	BACKWARD,
	LEFT,
	RIGHT,
	ROATE_UP,
	ROATE_DOWN,
	ROATE_LEFT,
	ROATE_RIGHT = 7
};

class CCPanoramaCamera;

typedef void(*CCPanoramaCameraCallback)(void* data, CCPanoramaCamera* cam);

//Panorama camera
class CCPanoramaCamera : public CCamera
{
public:
	//Camera panorama mode
	CCPanoramaCameraMode Mode = CCPanoramaCameraMode::Static;

	// Process input received from any keyboard-like input system, accepting input parameters in the form of camera-defined ENUM (abstracted from the windowing system)
	void ProcessKeyboard(CCameraMovement direction, float deltaTime);
	// Process input received from the mouse input system, predicting the offset in the x and y directions
	void ProcessMouseMovement(float xoffset, float yoffset, bool constrainPitch);
	// Process input received from the mouse wheel event
	void ProcessMouseScroll(float yoffset);
	//Zoom event
	void ProcessZoomChange(float precent);
	//Set mode
	void SetMode(CCPanoramaCameraMode mode);
	//Set rotation callback
	void SetRotateCallback(CCPanoramaCameraCallback callback, void* data);
	//Get zoom percentage
	float GetZoomPercentage();

	float RoateNearMax = 0.2f;
	float RoateFarMax = 3.5f;
	float ZoomSpeed = 0.05f;
	float FovMax = 170.0f;
	float FovMin = 2.0f;
	float RoateYForWorld = 0.0f;
	float RoateXForWorld = 0.0f;
	float MovementSpeed = DEF_SPEED;
	float RoateSpeed = DEF_ROATE_SPEED;
	float MouseSensitivity = DEF_SENSITIVITY;
	float OrthoSizeMin = 0.05f;
	float OrthoSizeMax = 1.0f;
	float OrthoSizeZoomSpeed = 0.001f;

private:
	CCPanoramaCameraCallback rotateCallback = nullptr;
	void* rotateCallbackData = nullptr;

	void CallRotateCallback();
};

