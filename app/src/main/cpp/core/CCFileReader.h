#pragma once
#ifndef VR720_CCFILEREADER_H
#define VR720_CCFILEREADER_H
#include "stdafx.h"

//File reader
class CCFileReader
{
public:
	/**
	 * Create a simple file reader
	 * @param path Target file path
	 */
	CCFileReader(std::string & path);
	CCFileReader();
	virtual ~CCFileReader();

	/**
	 * Get whether the file has been opened
	 * @return
	 */
	virtual bool Opened();
	/**
	 * Close file
	 */
	virtual void Close();

	/**
	 * Get file length
	 * @return File length
	 */
	virtual size_t Length();
	/**
	 * Move the fp pointer to the offset position at the beginning of the file
	 * @param i specified position
	 * @param seekType
	 */
	virtual void Seek(size_t i);
	/**
	 * Move fp pointer
	 * @param i specified position
	 * @param seekType Position type, SEEK_*
	 */
	virtual void Seek(size_t i, int seekType);
	/**
	 * Get file handle FILE*
	 * @return
	 */
	virtual FILE* Handle();

	/**
	 *
	 * @param arr buffer
	 * @param offset read offset
	 * @param count number to read
	 */
	virtual void Read(BYTE* arr, size_t offset, size_t count);
	/**
	 * Read one byte
	 * @return return byte
	 */
	virtual BYTE ReadByte();
	/**
	 * Read the entire file
	 * @param size Return buffer size
	 * @return Return buffer
	 */
	virtual BYTE* ReadAllByte(size_t *size);

protected:
	size_t len = 0;
private:
	FILE* file = nullptr;

	void CloseFileHandle();
};

#endif

