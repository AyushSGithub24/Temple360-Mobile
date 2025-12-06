#pragma once
#include "COpenGLRenderer.h"

typedef void(*ViewMouseCallback)(COpenGLView* view, float xpos, float ypos, int button, int type);
typedef void(*BeforeQuitCallback)(COpenGLView* view);

const int MAX_KEY_LIST = 8;

enum ViewMouseEventType {
	ViewMouseMouseDown,
	ViewMouseMouseUp,
	ViewMouseMouseMove,
	ViewMouseMouseWhell,
	ViewZoomEvent,
};

class CCamera;
class CCShader;
class CCRenderGlobal;
//OpenGL view abstract class
class COpenGLView
{
public:
	COpenGLView(COpenGLRenderer* renderer);
	COpenGLView() = default;

	virtual ~COpenGLView();

	//View height
	int Width = 800;
	//View width
	int Height = 600;

	/**
	 * Initialize
	 * @return Returns whether successful
	 */
	virtual bool Init() { return false; }
	/**
	 * Pause
	 */
	virtual void Pause() {}
	/**
	 * Resume
	 */
	virtual void Resume() {}

	/**
	 * Release
	 */
	virtual void Destroy() {}
	/**
	 * Manually update view size
	 * @param w width
	 * @param h height
	 */
	virtual void Resize(int w, int h);
	/**
	 * Set mouse event callback
	 */
	void SetMouseCallback(ViewMouseCallback mouseCallback);
	/**
	 * Set user zoom gesture event callback
	 */
	void SetZoomViewCallback(ViewMouseCallback mouseCallback);


	//Camera
	//**********************

	/**
	 * Calculate the matrix mapping of the current main camera
	 * @param shader The program to use
	 */
	void CalcMainCameraProjection(CCShader* shader) const;
	/**
	 * Calculate the matrix mapping without a camera
	 * @param shader The program to use
	 */
	void CalcNoMainCameraProjection(CCShader* shader) const;
	/**
	 * Calculate the matrix mapping of the current main camera
	 * @param shader The program to use
	 */
	void CalcCameraProjection(CCamera* camera, CCShader* shader, int w, int h) const;
	/**
	 * Calculate the matrix mapping of the current main camera (custom size)
	 * @param shader The program to use
	 * @param w screen width
	 * @param h screen height
	 */
	void CalcMainCameraProjectionWithWH(CCShader *shader, int w, int h) const;

	//Current main camera
	CCamera* Camera = nullptr;

	/**
	 * Set the current main camera
	 * @param camera Camera
	 */
	virtual void SetCamera(CCamera* camera);

	//Time
	//**********************

	/**
	 * Get the total drawing time of the current program
	 * @return
	 */
	virtual float GetTime() { return 0; }
	/**
	 * Get current FPS
	 * @return
	 */
	virtual float GetCurrentFps() { return 0; }
	/**
	 * Get drawing time
	 * @return
	 */
	virtual float GetDrawTime() { return 0; }
	/**
	 * Get delta time
	 * @return
	 */
	virtual float GetDeltaTime() { return 0; }

	//Keys
	//**********************

	/**
	 * Get whether a key is pressed
	 * @param code Key value
	 * @return
	 */
	virtual bool GetKeyPress(int code);
	/**
	 * Get whether a key is being pressed
	 * @param code Key value
	 * @return
	 */
	virtual bool GetKeyDown(int code);
	/**
	 * Get whether a key is released
	 * @param code Key value
	 * @return
	 */
	virtual bool GetKeyUp(int code);

	/**
	 * Get the current renderer
	 * @return
	 */
	COpenGLRenderer* GetRenderer();

	void SetManualDestroyCamera(bool manual);
protected:

	int DownedKeys[MAX_KEY_LIST] = { 0 };
	int UpedKeys[MAX_KEY_LIST]= { 0 };
	bool IsManualDestroyCamera = false;

	COpenGLRenderer* OpenGLRenderer = NULL;

	ViewMouseCallback scrollCallback = nullptr;
	ViewMouseCallback mouseCallback = nullptr;
	BeforeQuitCallback beforeQuitCallback = nullptr;

	int AddKeyInKeyList(int* list, int code);
	int IsKeyInKeyListExists(int* list, int code);
	void HandleDownKey(int code);
	void HandleUpKey(int code);

};

