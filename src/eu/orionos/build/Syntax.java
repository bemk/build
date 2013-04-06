package eu.orionos.build;

public class Syntax {
	public static final String MODULE_NAME = "name";

	public static final String GLOBAL_COMPILER = "compiler";
	public static final String GLOBAL_COMPILER_FLAGS = "compiler-flags";
	public static final String GLOBAL_LINKER = "linker";
	public static final String GLOBAL_LINKER_FLAGS = "linker-flags";
	public static final String GLOBAL_ARCHIVER = "archiver";
	public static final String GLOBAL_ARCHIVER_FLAGS = "archiver-flags";

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
	public static final String DYN_DEP = "dynamic-depend";

	public static final String SOURCE = "source-files";
	public static final String LINKED = "linked-file";
	public static final String ARCHIVED = "archived-file";

	public static final String LINK = "link";
}
