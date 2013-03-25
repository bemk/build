#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>

extern char** environ;

int main(int argc, char** argv)
{
	int p = fork();
	if (p == 0)
	{
		char* cmd[256];
		char command[2048];
		memset(cmd, 0, sizeof(cmd));
		memset(command, 0, sizeof(command));

		char pwd[256];
		getcwd(pwd, 256);
		strcat(pwd, "/build.jar");

		int i = 0;
		cmd[i++] = "-classpath";
		cmd[i++] = "eu.orionos.build.Build";
		cmd[i++] = "-jar";
		cmd[i++] = pwd;

		int ac = 1;

		for (; ac < argc; ac++, i++)
			cmd[i] = argv[ac];

		strcpy(command, "java ");
		for (i = 0; cmd[i] != 0; i++)
		{
			strcat(command, cmd[i]);
			strcat(command, " ");
		}
		system(command);
		exit (0);
	}
	else if (p < 0)
	{
		fprintf(stderr, "Problem: Could not spawn sub process!\n");
		exit(1);
	}
	else
	{
		int i = 0;
		wait(&i);
		if (i != 0)
		{
			fprintf(stderr, "Problem: Build failed!\n");
			exit(i);
		}
	}

	return EXIT_SUCCESS;
}
