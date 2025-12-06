#include "CCPanoramaCamera.h"

// Process input from any keyboard-like input system, accepting input parameters in the form of camera-defined ENUM (abstracted from the windowing system)
void CCPanoramaCamera::ProcessKeyboard(CCameraMovement direction, float deltaTime)
{
	float velocity = MovementSpeed * deltaTime;
	switch (direction) {
		case CCameraMovement::FORWARD:
			Position += GetFront() * velocity;
			break;
		case CCameraMovement::BACKWARD:
			Position -= GetFront() * velocity;
			break;
		case CCameraMovement::LEFT:
			Position -= GetFront() * velocity;
			break;
		case CCameraMovement::RIGHT:
			Position += GetFront() * velocity;
			break;
		case CCameraMovement::ROATE_UP: {
			glm::vec3 eulerAngles = GetEulerAngles();
			eulerAngles.y += RoateSpeed * deltaTime;
			SetEulerAngles(eulerAngles);
			CallRotateCallback();
			break;
		}
		case CCameraMovement::ROATE_DOWN: {
			glm::vec3 eulerAngles = GetEulerAngles();
			eulerAngles.y -= RoateSpeed * deltaTime;
			SetEulerAngles(eulerAngles);
			CallRotateCallback();
			break;
		}
		case CCameraMovement::ROATE_LEFT: {
			glm::vec3 eulerAngles = GetEulerAngles();
			eulerAngles.x -= RoateSpeed * 1.3f * deltaTime;
			SetEulerAngles(eulerAngles);
			CallRotateCallback();
			break;
		}
		case CCameraMovement::ROATE_RIGHT: {
			glm::vec3 eulerAngles = GetEulerAngles();
			eulerAngles.x += RoateSpeed * 1.3f * deltaTime;
			SetEulerAngles(eulerAngles);
			CallRotateCallback();
			break;
		}
		default:
			break;
	}
}

// Process input received from the mouse input system, predicting the offset in the x and y directions
void CCPanoramaCamera::ProcessMouseMovement(float xoffset, float yoffset, bool constrainPitch)
{
	switch (Mode)
	{
	case CCPanoramaCameraMode::CenterRoate: {
		xoffset *= MouseSensitivity;
		yoffset *= MouseSensitivity;

		glm::vec3 eulerAngles = GetEulerAngles();

		eulerAngles.x += xoffset;
		eulerAngles.y += yoffset;

		// Make sure the screen doesn't flip when the pitch is out of range
		if (constrainPitch)
		{
			if (eulerAngles.y > 89.0f)
				eulerAngles.y = 89.0f;
			if (eulerAngles.y < -89.0f)
				eulerAngles.y = -89.0f;
		}

		SetEulerAngles(eulerAngles);
		CallRotateCallback();
		break;
	}
	case CCPanoramaCameraMode::OutRoataround: {
		xoffset *= MouseSensitivity;
		yoffset *= MouseSensitivity;

		RoateXForWorld += xoffset;
		RoateYForWorld += yoffset;

		//Calculate the coordinates of the camera on this sphere
		float distance = glm::distance(Position, glm::vec3(0.0f));

		float w = distance * glm::cos(glm::radians(RoateYForWorld));
		Position.x = w * glm::cos(glm::radians(RoateXForWorld));
		Position.y = distance * glm::sin(glm::radians(RoateYForWorld));
		Position.z = w * glm::sin(glm::radians(RoateXForWorld));

		glm::vec3 eulerAngles = GetEulerAngles();

		if (Position.y < 0) eulerAngles.x = 180.0f - eulerAngles.x;
		if (Position.z < 0) eulerAngles.y = 180.0f - eulerAngles.y;

		SetEulerAngles(eulerAngles);

		break;
	}
	default:
		break;
	}
}

void CCPanoramaCamera::ProcessZoomChange(float precent) {
	switch (Mode) {
		case CCPanoramaCameraMode::CenterRoate: {
			FiledOfView = (precent * (FovMax - FovMin)) + FovMin;
			if (fovChangedCallback)
				fovChangedCallback(fovChangedCallbackData, FiledOfView);
			break;
		}
		case CCPanoramaCameraMode::OutRoataround: {
			Position.z -= (precent * (RoateFarMax - RoateNearMax)) + RoateNearMax;
			break;
		}
		case CCPanoramaCameraMode::OrthoZoom: {

			OrthographicSize = (precent * (OrthoSizeMax - OrthoSizeMin)) + OrthoSizeMin;
			if (orthoSizeChangedCallback)
				orthoSizeChangedCallback(orthoSizeChangedCallbackData, OrthographicSize);
			break;
		}
		case CCPanoramaCameraMode::Static:
			break;
	}
}

// Process input received from the mouse wheel event
void CCPanoramaCamera::ProcessMouseScroll(float yoffset)
{
	switch (Mode)
	{
	case CCPanoramaCameraMode::CenterRoate: {
		if (FiledOfView >= FovMin && FiledOfView <= FovMax)
			FiledOfView -= yoffset * ZoomSpeed;
		if (FiledOfView <= FovMin) FiledOfView = FovMin;
		if (FiledOfView >= FovMax) FiledOfView = FovMax;
		if (fovChangedCallback)
			fovChangedCallback(fovChangedCallbackData, FiledOfView);
		break;
	}
	case CCPanoramaCameraMode::OutRoataround: {
		Position.z -= yoffset * ZoomSpeed * 0.1f;
		if (Position.z < RoateNearMax) Position.z = RoateNearMax;
		if (Position.z > RoateFarMax) Position.z = RoateFarMax;
		break;
	}
	case CCPanoramaCameraMode::OrthoZoom: {
		if (OrthographicSize >= OrthoSizeMin && OrthographicSize <= OrthoSizeMax)
			OrthographicSize -= yoffset * OrthoSizeZoomSpeed;
		if (OrthographicSize <= OrthoSizeMin) OrthographicSize = OrthoSizeMin;
		if (OrthographicSize >= OrthoSizeMax) OrthographicSize = OrthoSizeMax;
		if (orthoSizeChangedCallback)
			orthoSizeChangedCallback(orthoSizeChangedCallbackData, OrthographicSize);
		break;
	}
	case CCPanoramaCameraMode::Static:
		break;
	}
}

//Set mode
void CCPanoramaCamera::SetMode(CCPanoramaCameraMode mode)
{
	Mode = mode;
	switch (Mode)
	{
	case CCPanoramaCameraMode::CenterRoate:
		Reset();
		break;
	case CCPanoramaCameraMode::OutRoataround:
		Reset();
		Position = glm::vec3(0.0f, 0.0f, 3.0f);
		SetEulerAngles(glm::vec3(-90.0f, 0.0f, 0.0f));
		RoateYForWorld = 0.0f;
		RoateXForWorld = 0.0f;
		ForceUpdate();
		break;
	case CCPanoramaCameraMode::Static:
		Reset();
		break;
	case CCPanoramaCameraMode::OrthoZoom:
		Reset();
		Position = glm::vec3(0.0f, 0.0f, 0.2f);
		ForceUpdate();
		break;
	default:
		break;
	}
	if (Projection == CCameraProjection::Orthographic) {
		if (orthoSizeChangedCallback)
			orthoSizeChangedCallback(orthoSizeChangedCallbackData, OrthographicSize);
	}
	else {
		if (fovChangedCallback)
			fovChangedCallback(fovChangedCallbackData, FiledOfView);
	}
}

void CCPanoramaCamera::SetRotateCallback(CCPanoramaCameraCallback callback, void* data)
{
	rotateCallback = callback;
	rotateCallbackData = data;
}

void CCPanoramaCamera::CallRotateCallback() {
	if (rotateCallback) rotateCallback(rotateCallbackData, this);
}

float CCPanoramaCamera::GetZoomPercentage() {
	switch (Mode) {
		case CCPanoramaCameraMode::OrthoZoom:
			return (float)(OrthographicSize - OrthoSizeMin) / (float)(OrthoSizeMax - OrthoSizeMin);
		case CCPanoramaCameraMode::CenterRoate:
			return (float)(FiledOfView - RoateNearMax) / (float)(FovMax - FovMin);
		case CCPanoramaCameraMode::OutRoataround:
			return (float)(Position.z - RoateNearMax) / (float)(RoateFarMax - RoateNearMax);
		case CCPanoramaCameraMode::Static:
			return 1.0f;
	}
	return 0;
}
