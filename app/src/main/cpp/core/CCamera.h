#pragma once
#ifndef VR720_CCAMER_H
#define VR720_CCAMER_H
#include "stdafx.h"
#include <gtc/matrix_transform.hpp>
#include <vector>
#include "CColor.h"
#include "CCModel.h"

// Initialize camera variables
const float DEF_YAW = 0.0f;
const float DEF_PITCH = 0.0f;
const float DEF_SPEED = 2.5f;
const float DEF_ROATE_SPEED = 20.0f;
const float DEF_SENSITIVITY = 0.1f;
const float DEF_FOV = 45.0f;

/**
 * Camera projection mode
 */
enum class CCameraProjection {
	/**
	 * Perspective projection
	 */
	Perspective,
	/**
	 * Orthographic projection
	 */
	Orthographic
};

/**
 * Callback when the camera FOV changes
 */
typedef void(*CCPanoramaCameraFovChangedCallback)(void* data, float fov);

class COpenGLView;
class CCModel;
/**
 * Camera class, processes input and calculates corresponding Euler angles, vectors and matrices
 */
class CCamera : public CCModel
{
public:
	//Camera projection
	CCameraProjection Projection = CCameraProjection::Perspective;
	// Camera FOV
	float FiledOfView = DEF_FOV;
	//The size of the orthographic projection camera view in the vertical direction
	float OrthographicSize = 1.0f;
	//Clipping plane near end
	float ClippingNear = 0.1f;
	//Clipping plane far end
	float ClippingFar = 1000.0f;
	//Camera background color
	CColor Background = CColor::Black;

	//Camera view matrix
	glm::mat4 view = glm::mat4(1.0f);
	//Camera perspective matrix
	glm::mat4 projection = glm::mat4(1.0f);

	/**
	 * Initialize camera
	 * @param position Position
	 * @param up Up vector
	 * @param rotate Rotation
	 */
	CCamera(glm::vec3 position = glm::vec3(0.0f, 0.0f, 0.0f), glm::vec3 up = glm::vec3(0.0f, 1.0f, 0.0f), glm::vec3 rotate = glm::vec3(0.0f, 0.0f, 0.0f));

	/**
	 * Returns the view matrix calculated using Euler angles and the LookAt matrix
	 * @return
	 */
	glm::mat4 GetViewMatrix();

	/**
	 * Set the callback when the camera perspective projection FOV changes
	 * @param callback callback
	 * @param data custom callback parameters
	 */
	void SetFOVChangedCallback(CCPanoramaCameraFovChangedCallback callback, void* data);
	/**
	 * Set the callback when the camera orthographic projection size changes
	 * @param callback callback
	 * @param data custom callback parameters
	 */
	void SetOrthoSizeChangedCallback(CCPanoramaCameraFovChangedCallback callback, void* data);

	/**
	 * Set camera fov
	 * @param fov FieldOfView
	 */
	void SetFOV(float fov);
	/**
	 * Set the camera orthographic projection size
	 * @param o Orthographic projection size (based on screen width)
	 */
	void SetOrthoSize(float o);

	/**
	 * Force refresh camera
	 */
	void ForceUpdate();
	/**
	 * Reset camera rotation and position
	 */
	void Reset() override ;

	/**
	 * Set the VIEW to which the camera belongs
	 * @param view
	 */
	void SetView(COpenGLView* view);

	/**
	 * @brief Convert window coordinates to world coordinates
	 * @brief screenPoint window coordinate point
	 * @brief viewportRange Viewport range. The values are in order: top-left, bottom-right
	 * @brief modelViewMatrix Model view matrix
	 * @brief projectMatrix projection matrix
	 * @brief pPointDepth The depth of the screen point. If not specified (it is nullptr), the depth value is read from the depth buffer
	 * @return world coordinate system
	 * @note Note: The obtained world coordinate system must be divided by the homogeneous coordinate value w before use.
	 * If w is 0, this point should not be used.
	 * @code
	 *  // sample
	 *  ...
	 *  auto&& worldPoint = Screen2World(...);
	 *  if( !FuzzyIsZero( worldPoint.w ) )
	 *  {
	 *
	 * glm::vec3 world3D(worldPoint);
	 *      world3D /= worldPoint;
	 *      /// using world3D
	 *
	 * }
	 *
	 * else
	 *
	 * {
	 *
	 * // error handler
	 *
	 * }
	 */
	glm::vec3 Screen2World(const glm::vec2& screenPoint, glm::mat4& model, const float* pPointDepth);
	/**
	 * @brief Convert world coordinate system to screen coordinate system
	 * @brief worldPoint Point coordinates of world coordinates
	 * @brief viewportRange Viewport range. The values are in order: top-left, bottom-right
	 * @brief modelViewMatrix Model view matrix
	 * @brief projectMatrix projection matrix
	 * @brief pPointDepth The depth of the screen point. If not specified (it is nullptr), the depth value is read from the depth buffer
	 * @return Window coordinate point
	 * @note The returned window coordinates have a depth value. If you only want to use 2D window pixel coordinate points, just use its x and y dimensions.
	 */
	glm::vec3 World2Screen(const glm::vec3& worldPoint, glm::mat4& model);

	/**
	 * Set the camera orientation
	 * @param faceIndex 0-6, a total of six faces
	 */
	void SwitchToFace(int faceIndex);

protected:
	COpenGLView* glView = nullptr;

	CCPanoramaCameraFovChangedCallback fovChangedCallback = nullptr;
	void* fovChangedCallbackData = nullptr;
	CCPanoramaCameraFovChangedCallback orthoSizeChangedCallback = nullptr;
	void* orthoSizeChangedCallbackData = nullptr;
};

#endif
