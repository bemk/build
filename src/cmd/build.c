/* Build - Hopefully a simple build system
 * Copyright (C) 2013 - Bart Kuivenhoven
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * General Public License for more details.
 *
 * should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA. 
 *
 * A version of the licence can also be found at http://gnu.org/licences/
 */

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
