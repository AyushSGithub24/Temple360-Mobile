#include "stdafx.h"
#include "GlUtils.h"

/**
 * @brief One-dimensional linear mapping function, which maps point x in [a,b] to point x1 in [a1,b1]
 *
 **/
float LinearMap(float x, float a, float b, float a1, float b1)
{
	auto srcDelt(b - a);
	if (FuzzyIsZero(srcDelt))
	{
		// The original range is 0,
		//LOG_WARN("The original range cannot be 0(%f,%f)", a, b);
		return a1;
	}

	return (b1 - a1) / srcDelt * (x - a) + a1;
}