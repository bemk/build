#include <stdio.h>
#include <stdlib.h>
#include <test.h>

int main (int argc, char** argv)
{
	printf("Just testing the build system!\n");
	minimal();
#ifdef ALPHA
	alpha();
#endif
#ifdef BETA
	beta();
#endif
	
	return EXIT_SUCCESS;
}
