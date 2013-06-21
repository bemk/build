/*  Build - Hopefully a simple build system
    Copyright (C) 2013 - Bart Kuivenhoven

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA. 

    A version of the licence can also be found at http://gnu.org/licences/
*/

package eu.orionos.build;

public class Semantics {
	public static final String MODULE_NAME = "name";

	public static final String GLOBAL_COMPILER = "compiler";
	public static final String GLOBAL_COMPILER_FLAGS = "compiler-flags";
	public static final String GLOBAL_COMPILER_OVERRIDE_FLAGS = "compiler-override-flags";
	public static final String GLOBAL_LINKER = "linker";
	public static final String GLOBAL_LINKER_FLAGS = "linker-flags";
	public static final String GLOBAL_LINKER_OVERRIDE_FLAGS = "linker-override-flags";
	public static final String GLOBAL_ARCHIVER = "archiver";
	public static final String GLOBAL_ARCHIVER_FLAGS = "archiver-flags";
	public static final String GLOBAL_ARCHIVER_OVERRIDE_FLAGS = "archiver-override-flags";

	public static final String GLOBAL_DEFS = "global";

	public static final String MOD_COMPILER = "mcompiler";
	public static final String MOD_COMPILER_FLAGS = "mcompiler-flags";
	public static final String MOD_LINKER = "mlinker";
	public static final String MOD_LINKER_FLAGS = "mlinker-flags";
	public static final String MOD_ARCHIVER = "marchiver";
	public static final String MOD_ARCHIVER_FLAGS = "marchiver-flags";

	public static final String DYN_ARCHIVER_FLAGS = "darchiver-flags";
	public static final String DYN_COMPILER_FLAGS = "dcompiler-flags";
	public static final String DYN_LINKER_FLAGS = "dlinker-flags";
	
	public static final String DYN_MOD_COMPILER_FLAGS = "mdcompiler-flags";
	public static final String DYN_MOD_LINKER_FLAGS = "mdcompiler-flags";
	public static final String DYN_MOD_ARCHIVER_FLAGS = "mdarchiver-flags";

	public static final String DEP = "depend";
	public static final String DYN_DEP = "ddepend";
	public static final String DEP_PATH = "path";

	public static final String SOURCE = "source-files";
	public static final String LINKED = "linked-file";
	public static final String ARCHIVED = "archived-file";

	public static final String LINK = "link";
	public static final String ARCHIVE = "archive";

	public static final String CONFIG_GLOBAL_KEY = "key";
	public static final String CONFIG_GLOBAL_FLAGS = "flags";
	public static final String CONFIG_BUILD_DIR = "build-dir";

	public static final String FLAG_DEP_SET = "set";
	public static final String FLAG_DEP_ENUM = "enum";

	public static final String FLAG_DEP_INFO = "info";
	public static final String FLAG_DEP_MANDATORY = "mandatory";
}
