#pragma once
#include "stdafx.h"


class COpenGLView;
/**
 * Renderer abstract class
 */
class COpenGLRenderer
{
protected:
	//Width and height
	int Width = 0, Height = 0;

public:
	COpenGLRenderer();
	~COpenGLRenderer();

	int GetWidth() const;
	int GetHeight() const;

	/**
	 * Reinitialize
	 * @return Returns whether the initialization was successful
	 */
	virtual bool ReInit();
	/**
	 * Initialize
	 * @return Returns whether the initialization was successful
	 */
	virtual bool Init();
	/**
	 * Called when rendering
	 * @param FrameTime Delta time
	 */
	virtual void Render(float FrameTime);
	/**
	 * Called when rendering UI
	 */
	virtual void RenderUI();
	/**
	 * Called every frame update
	 */
	virtual void Update();
	/**
	 * This method is called when the view is resized
	 * @param Width New width
	 * @param Height New height
	 */
	virtual void Resize(int Width, int Height);
	/**
	 * Release
	 */
	virtual void Destroy();
	/**
	 * Delayed release
	 */
	virtual void MarkDestroy();

	/**
	 * Owner View
	 */
	COpenGLView * View = nullptr;
};

