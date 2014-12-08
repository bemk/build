/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013 - 2014
        Toon Schoenmakers  <nighteyes1993@gmail.com>  - 2013
        Steven vd Schoot   <stevenvdschoot@gmail.com> - 2013

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

import eu.orionos.build.compilePhase.BuildPhase;
import eu.orionos.build.compilePhase.PhaseStart;
import eu.orionos.build.makefile.MakeModule;
import eu.orionos.build.ui.CLIDebug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Module {

	private ArrayList<Module> subModules = new ArrayList<Module>();
	private HashMap<String, Module> dynamicModules = new HashMap<String, Module>();
	private ArrayList<String> sourceFiles = new ArrayList<String>();
	private String linkedFile;
	private String archivedFile;
	private String cwd;
	private String name;
	private Module parent;
	private JSONObject module;
	private BuildPhase buildState;

	private MakeModule makefile = new MakeModule("module-x", this);

	private boolean toLink = false;
	private boolean toArchive = false;
	private boolean done = false;

	private String globalCompiler;
	private String globalLinker;
	private String globalArchiver;

	private String modCompiler;
	private String modLinker;
	private String modArchiver;

	private String globalCompilerFlags;
	private String globalLinkerFlags;
	private String globalArchiverFlags;

	private String globalOverrideCompilerFlags;
	private String globalOverrideLinkerFlags;
	private String globalOverrideArchiverFlags;

	private String modCompilerFlags;
	private String modLinkerFlags;
	private String modArchiverFlags;

	private JSONArray dynArchiverFlags;
	private JSONArray dynCompilerFlags;
	private JSONArray dynLinkerFlags;

	private JSONArray dynModArchiverFlags;
	private JSONArray dynModCompilerFlags;
	private JSONArray dynModLinkerFlags;

	public Module(String path) throws Exception {
		this(path, null);
	}

	public String getName() {
		return this.name;
	}

	public Module(String path, Module parent) throws Exception {
		/* Get some verbosity out of our system */
		if (Config.getInstance().verbose()) {
			System.out.println("Parsing " + path);
		}
		this.parent = parent;
		/* Get the actual module file */
		File f = new File(path);
		if (!f.exists()) {
			System.err.println("Module at " + path + " can not be found");
			if (parent != this && parent != null)
				System.err
						.println("This dependency could be written incorrectly in "
								+ parent.name);
			else
				System.err
						.println("Maybe you misspelled the name of the build file. If none exists, run build --gen-module");
			System.exit(ErrorCode.FILE_NOT_FOUND);
		}
		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
		}
		reader.close();
		try {
			module = new JSONObject(stringBuilder.toString());
		} catch (JSONException e1) {
			System.err.println("Failed to parse JSON string");
			e1.printStackTrace();
		}
		/* Get some paths right */
		this.cwd = f.getAbsolutePath();
		int len = 0;
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			len = this.cwd.lastIndexOf('\\');
		else
			len = this.cwd.lastIndexOf('/');
		this.cwd = this.cwd.substring(0, len);

		try {
			this.name = module.getString("name");
		} catch (JSONException e) {
			System.err.println("Module in " + cwd + "referenced by " + path
					+ " does not have a name field!");
			System.err.println("Modules have to have a name field!");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		if (Config.getInstance().RegisterModule(this) == false) {
			System.err.println("Module with name: " + this.name
					+ " conflicts with another module of the same name");
			System.exit(ErrorCode.NAME_CONFLICT);
		}

		makefile.setTarget(this.name);
		
		/* Determine whether or not we should link */
		try {
			this.toLink = module.getBoolean(Semantics.LINK);
			if (this.toLink == true)
				Config.getInstance().set_ld_required();
		} catch (JSONException e) {
			System.err.println("Module " + name + " Did not specify linking");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		/* And determine the same for archiving */
		try {
			this.toArchive = module.getBoolean(Semantics.ARCHIVE);
			if (this.toArchive == true)
				Config.getInstance().set_ar_required();
		} catch (JSONException e) {
			System.err.println("Module " + name + " did not specify archiving");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}

		/* Read other global stuff into local variables for easier access */
		this.globalCompiler = module.optString(Semantics.GLOBAL_COMPILER, null);
		if (this.getGlobalCompiler() == null) {
			System.err
					.println("A global compiler has to be set in the main build file");
			System.err.println("Specify: \"" + Semantics.GLOBAL_COMPILER
					+ "\" : \"<compiler>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalCompilerFlags = module.optString(
				Semantics.GLOBAL_COMPILER_FLAGS, null);
		if (this.getGlobalCompilerFlags() == null) {
			System.err
					.println("Global compiler options must be set in the main build file");
			System.err.println("Specify: \"" + Semantics.GLOBAL_COMPILER_FLAGS
					+ "\" : \"<compiler flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalOverrideCompilerFlags = module.optString(
				Semantics.GLOBAL_COMPILER_OVERRIDE_FLAGS, null);
		this.globalLinker = module.optString(Semantics.GLOBAL_LINKER, null);
		if (this.getGlobalLinker() == null) {
			System.err
					.println("A Global linker must be set in the main build file");
			System.err.println("Specify: \"" + Semantics.GLOBAL_LINKER
					+ "\" : \"<linker>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalLinkerFlags = module.optString(
				Semantics.GLOBAL_LINKER_FLAGS, null);
		if (this.getGlobalLinkerFlags() == null) {
			System.err
					.println("Global Linker options must be set in the main build file");
			System.err.println("Specify: \"" + Semantics.GLOBAL_LINKER_FLAGS
					+ "\" : \"<linker flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalOverrideLinkerFlags = module.optString(
				Semantics.GLOBAL_LINKER_OVERRIDE_FLAGS, null);
		this.globalArchiver = module.optString(Semantics.GLOBAL_ARCHIVER, null);
		if (this.getGlobalArchiver() == null
				&& Config.getInstance().get_ar_required()) {
			System.err
					.println("A global archiver must be set in the main build file");
			System.err.println("Specify: \"" + Semantics.GLOBAL_ARCHIVER
					+ "\" : \"<archiver>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalArchiverFlags = module.optString(
				Semantics.GLOBAL_ARCHIVER_FLAGS, null);
		if (this.getGlobalArchiverFlags() == null
				&& Config.getInstance().get_ar_required()) {
			System.err
					.println("Global archiver flags must be set in the main build file");
			System.err.println("Specify: \"" + Semantics.GLOBAL_ARCHIVER_FLAGS
					+ "\" : \"<archiver flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalOverrideArchiverFlags = module.optString(
				Semantics.GLOBAL_ARCHIVER_OVERRIDE_FLAGS, null);

		/* Determine the linked output file */
		if (this.toLink) {
			try {
				this.linkedFile = module.getString(Semantics.LINKED);
			} catch (JSONException e) {
				System.err.println("Module " + name
						+ " is to link, but no output file specified");
				System.err.println("Specify: \"" + Semantics.LINKED
						+ "\" : \"<outputfile>\"");
				System.exit(ErrorCode.PARSE_FAILED);
			}
		}
		if (this.toArchive) {
			try {
				this.archivedFile = module.getString(Semantics.ARCHIVED);
			} catch (JSONException e) {
				System.err.println("Module " + name
						+ " is to be archived, but no output file specified");
				System.err.println("Specify: \"" + Semantics.ARCHIVED
						+ "\" : \"<outputfile>\"");
				System.exit(ErrorCode.PARSE_FAILED);
			}
		}

		/* Get all the modular data in place */
		this.modCompiler = module.optString(Semantics.MOD_COMPILER, null);
		this.modCompilerFlags = module.optString(Semantics.MOD_COMPILER_FLAGS,
				null);
		this.modArchiverFlags = module.optString(Semantics.MOD_ARCHIVER_FLAGS,
				null);
		this.modArchiver = module.optString(Semantics.MOD_ARCHIVER, null);
		this.modArchiverFlags = module.optString(Semantics.MOD_ARCHIVER_FLAGS,
				null);
		this.modLinker = module.optString(Semantics.MOD_LINKER, null);
		this.modLinkerFlags = module
				.optString(Semantics.MOD_LINKER_FLAGS, null);

		/* The dynamic flags */
		this.dynArchiverFlags = module
				.optJSONArray(Semantics.DYN_ARCHIVER_FLAGS);
		this.dynCompilerFlags = module
				.optJSONArray(Semantics.DYN_COMPILER_FLAGS);
		this.dynLinkerFlags = module.optJSONArray(Semantics.DYN_LINKER_FLAGS);

		/* And the dynamic module wide flags */
		this.dynModArchiverFlags = module
				.optJSONArray(Semantics.DYN_MOD_ARCHIVER_FLAGS);
		this.dynModCompilerFlags = module
				.optJSONArray(Semantics.DYN_MOD_COMPILER_FLAGS);
		this.dynModLinkerFlags = module
				.optJSONArray(Semantics.DYN_MOD_LINKER_FLAGS);
		/* Find all source files for this module */
		JSONArray sources = module.optJSONArray(Semantics.SOURCE);
		for (int i = 0; i < sources.length(); i++) {
			final String source = sources.optString(i, null);
			if (source != null)
				this.sourceFiles.add(source);
		}

		/* Get all the dependencies, dynamic or not */
		JSONArray array = module.optJSONArray(Semantics.DEP);
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				final StringBuilder modPath = new StringBuilder();
				try {
					final JSONObject o = array.getJSONObject(i);
					modPath.append(cwd);
					modPath.append('/');
					modPath.append(o.getString(Semantics.DEP_PATH));
					subModules.add(new Module(modPath.toString(), this));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		array = module.optJSONArray(Semantics.DYN_DEP);
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				final StringBuilder modPath = new StringBuilder();
				try {
					JSONObject o = array.getJSONObject(i);
					modPath.append(cwd);
					modPath.append('/');
					modPath.append(o.getString(Semantics.DEP_PATH));
					dynamicModules.put(
							o.getString(Semantics.CONFIG_GLOBAL_KEY),
							new Module(modPath.toString(), this));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		buildState = new BuildPhase(this);
	}

	/*
	 * These functions retrieve the global values only. These are helper
	 * functions and should only be called by the functions that determine the
	 * actual values
	 */
	protected String getGlobalArchiver() {
		if (parent != null && this.globalArchiver == null)
			return parent.getGlobalArchiver();
		return globalArchiver;
	}

	protected String getGlobalCompiler() {
		if (parent != null && globalCompiler == null)
			return parent.getGlobalCompiler();
		return globalCompiler;
	}

	protected String getGlobalLinker() {
		if (parent != null && globalLinker == null)
			return parent.getGlobalLinker();
		return globalLinker;
	}

	protected String getGlobalArchiverFlags() {
		final StringBuilder builder = new StringBuilder();
		if (globalOverrideArchiverFlags == null
				|| globalOverrideArchiverFlags.isEmpty()) {
			if (parent != null)
				builder.append(parent.getGlobalArchiverFlags());
			else
				builder.append(Config.getInstance().aflags());
		} else {
			builder.append(globalOverrideArchiverFlags);
		}
		if (globalArchiverFlags != null && !globalArchiverFlags.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(globalArchiverFlags);
		}
		String dyn = getDynArchiverFlags();
		if (!dyn.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(dyn);
		}
		return builder.toString();
	}

	protected String getGlobalCompilerFlags() {
		final StringBuilder builder = new StringBuilder();
		if (globalOverrideCompilerFlags == null
				|| globalOverrideCompilerFlags.isEmpty()) {
			if (parent != null)
				builder.append(parent.getGlobalCompilerFlags());
			else
				builder.append(Config.getInstance().cflags());
		} else {
			builder.append(globalOverrideCompilerFlags);
		}
		if (globalCompilerFlags != null && !globalCompilerFlags.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(globalCompilerFlags);
		}
		String dyn = getDynCompilerFlags();
		if (!dyn.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(dyn);
		}
		return builder.toString();
	}

	protected String getGlobalLinkerFlags() {
		final StringBuilder builder = new StringBuilder();
		if (globalOverrideLinkerFlags == null
				|| globalOverrideLinkerFlags.isEmpty()) {
			if (parent != null)
				builder.append(parent.getGlobalLinkerFlags());
			else
				builder.append(Config.getInstance().ldflags());
		} else {
			builder.append(globalOverrideLinkerFlags);
		}
		if (globalLinkerFlags != null && !globalLinkerFlags.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(globalLinkerFlags);
		}
		String dyn = getDynLinkerFlags();
		if (!dyn.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(dyn);
		}
		return builder.toString();
	}

	/* And get the actual strings to insert into the commands */
	public String getCompiler() {
		if (modCompiler == null)
			return getGlobalCompiler();
		return modCompiler;
	}

	private String getDynCompilerFlags() {
		final StringBuilder builder = new StringBuilder();
		Config c = Config.getInstance();

		if (dynCompilerFlags != null) {
			for (int i = 0; i < dynCompilerFlags.length(); i++) {
				try {
					final JSONObject o = dynCompilerFlags.getJSONObject(i);
					final String key = o.getString(Semantics.CONFIG_GLOBAL_KEY);
					if (c.getDefined(key) && o.has(Semantics.CONFIG_GLOBAL_KEY)) {
						if (builder.length() > 0)
							builder.append(' ');
						builder.append(o
								.getString(Semantics.CONFIG_GLOBAL_FLAGS));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return builder.toString();
	}

	private String getDynModCompilerFlags() {
		final StringBuilder builder = new StringBuilder();
		Config c = Config.getInstance();

		if (dynModCompilerFlags != null) {
			for (int i = 0; i < dynModCompilerFlags.length(); i++) {
				try {
					final JSONObject o = dynModCompilerFlags.getJSONObject(i);
					final String key = o.getString(Semantics.CONFIG_GLOBAL_KEY);
					CLIDebug.getInstance().writeline(
							"Dynamic modular cc flags found: " + key
									+ " in module " + name);
					if (c.getDefined(key)
							&& o.has(Semantics.CONFIG_GLOBAL_FLAGS)) {
						if (builder.length() > 0)
							builder.append(' ');
						builder.append(o
								.getString(Semantics.CONFIG_GLOBAL_FLAGS));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			CLIDebug.getInstance().writeline(
					"No dynamic modular cc flags found in " + name);
		}
		return builder.toString();
	}

	public String[] getCompilerFlags() {
		final String cflags = getGlobalCompilerFlags();
		if (cflags == null)
			return null;
		final StringBuilder builder = new StringBuilder(cflags);
		if (modCompilerFlags != null) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(modCompilerFlags);
		}

		String dyn = getDynModCompilerFlags();
		if (!dyn.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(dyn);
		}
		return builder.toString().split(" ");
	}

	public String getArchiver() {
		if (modArchiver == null)
			return getGlobalArchiver();
		return modArchiver;
	}

	private String getDynArchiverFlags() {
		final StringBuilder builder = new StringBuilder();
		Config c = Config.getInstance();

		if (dynArchiverFlags != null) {
			for (int i = 0; i < dynArchiverFlags.length(); i++) {
				try {
					final JSONObject o = dynArchiverFlags.getJSONObject(i);
					final String key = o.getString(Semantics.CONFIG_GLOBAL_KEY);
					if (c.getDefined(key)
							&& o.has(Semantics.CONFIG_GLOBAL_FLAGS)) {
						if (builder.length() > 0)
							builder.append(' ');
						builder.append(o
								.getString(Semantics.CONFIG_GLOBAL_FLAGS));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return builder.toString();
	}

	private String getDynModArchiverFlags() {
		final StringBuilder builder = new StringBuilder();
		Config c = Config.getInstance();
		if (dynModArchiverFlags != null) {
			for (int i = 0; i < dynModArchiverFlags.length(); i++) {
				try {
					final JSONObject o = dynModArchiverFlags.getJSONObject(i);
					final String key = o.getString(Semantics.CONFIG_GLOBAL_KEY);
					if (c.getModuleDefined(this.name, key)
							&& o.has(Semantics.CONFIG_GLOBAL_FLAGS)) {
						if (builder.length() > 0)
							builder.append(' ');
						builder.append(o
								.getString(Semantics.CONFIG_GLOBAL_FLAGS));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return builder.toString();
	}

	public String[] getArchiverFlags() {
		final String aflags = getGlobalArchiverFlags();
		if (aflags == null)
			return null;
		final StringBuilder builder = new StringBuilder(aflags);
		if (modArchiverFlags != null) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(modArchiverFlags);
		}

		final String dyn = getDynModArchiverFlags();
		if (!dyn.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(dyn);
		}
		return builder.toString().split(" ");
	}

	public String getLinker() {
		if (modLinker == null)
			return getGlobalLinker();
		return modLinker;
	}

	private String getDynLinkerFlags() {
		final StringBuilder builder = new StringBuilder();
		Config c = Config.getInstance();

		if (dynLinkerFlags != null) {
			for (int i = 0; i < dynLinkerFlags.length(); i++) {
				try {
					final JSONObject o = dynLinkerFlags.getJSONObject(i);
					final String key = o.getString(Semantics.CONFIG_GLOBAL_KEY);
					if (c.getModuleDefined(this.name, key)
							&& o.has(Semantics.CONFIG_GLOBAL_FLAGS)) {
						if (builder.length() > 0)
							builder.append(' ');
						builder.append(o
								.getString(Semantics.CONFIG_GLOBAL_FLAGS));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return builder.toString();
	}

	private String getDynModLinkerFlags() {
		final StringBuilder builder = new StringBuilder();
		Config c = Config.getInstance();

		if (dynModLinkerFlags != null) {
			for (int i = 0; i < dynModLinkerFlags.length(); i++) {
				try {
					final JSONObject o = dynModLinkerFlags.getJSONObject(i);
					final String key = o.getString(Semantics.CONFIG_GLOBAL_KEY);
					if (c.getModuleDefined(this.name, key)
							&& o.has(Semantics.CONFIG_GLOBAL_FLAGS)) {
						if (builder.length() > 0)
							builder.append(' ');
						builder.append(o
								.getString(Semantics.CONFIG_GLOBAL_FLAGS));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return builder.toString();
	}

	public String[] getLinkerFlags() {
		final String lflags = getGlobalLinkerFlags();
		if (lflags == null)
			return null;
		final StringBuilder builder = new StringBuilder(lflags);
		if (modLinkerFlags != null) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(modLinkerFlags);
		}

		String dyn = getDynModLinkerFlags();
		if (!dyn.isEmpty()) {
			if (builder.length() > 0)
				builder.append(' ');
			builder.append(dyn);
		}
		return builder.toString().split(" ");
	}

	/* helper function for determining the actual dependencies */
	private void addDynamicDeps(ArrayList<Module> dependencies, JSONArray array) {
		for (int i = 0; i < array.length(); i++) {
			try {
				final String flag = array.getString(i);
				if (dynamicModules.containsKey(flag)) {
					Module m = dynamicModules.get(flag);
					if (!dependencies.contains(m)) {
						dependencies.add(m);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/* Use this to calculate the dependencies to wait for, and to make build */
	public ArrayList<Module> calculateDependencies() {
		ArrayList<Module> dependencies = new ArrayList<Module>(subModules);
		JSONArray flags = Config.getInstance().getGlobalFlags();
		if (flags != null) {
			addDynamicDeps(dependencies, flags);
		}
		flags = Config.getInstance().getModuleFlags(name);
		if (flags != null) {
			addDynamicDeps(dependencies, flags);
		}
		return dependencies;
	}

	/*
	 * Initialise build phase, make all dependencies compile, and then do the
	 * same
	 */
	public void build() /* throws InterruptedException */
	{
		buildState.switchPhase(new PhaseStart(buildState));
	}

	protected Set<String> getDynamicBuildFlags(JSONArray flaglist) {
		Set<String> set = new HashSet<String>();
		if (flaglist == null)
			return set;
		for (int i = 0; i < flaglist.length(); i++) {
			try {
				JSONObject o = flaglist.getJSONObject(i);
				String key = o.getString(Semantics.CONFIG_GLOBAL_KEY);
				set.add(key);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return set;
	}

	public Set<String> getBuildFlags() {
		Set<String> flags = new HashSet<String>();

		/* Figure out dependencies */
		Set<Module> deps = new HashSet<Module>(subModules);
		deps.addAll(dynamicModules.values());

		/* Ask all dependencies for their flags */
		Iterator<Module> i = deps.iterator();
		Set<String> tmp = new HashSet<String>();
		while (i.hasNext()) {
			Module m = i.next();
			tmp.addAll(m.getBuildFlags());
		}

		/* Add our own flags to the set */
		flags.addAll(dynamicModules.keySet());
		flags.addAll(getDynamicBuildFlags(dynArchiverFlags));
		flags.addAll(getDynamicBuildFlags(dynCompilerFlags));
		flags.addAll(getDynamicBuildFlags(dynLinkerFlags));
		flags.addAll(getDynamicBuildFlags(dynModArchiverFlags));
		flags.addAll(getDynamicBuildFlags(dynModCompilerFlags));
		flags.addAll(getDynamicBuildFlags(dynModLinkerFlags));
		flags.addAll(tmp);

		return flags;
	}

	/* Generate a list of output files to be fed into the linker */
	public ArrayList<String> getLinkableFiles() {
		/* TODO: generate a list of all files to be linked */
		ArrayList<String> ret = new ArrayList<String>();
		ArrayList<Module> deps = calculateDependencies();
		if (this.toArchive) {
			ret.add(getAFile());
		} else {
			ret.addAll(buildState.filesForArchive());
			/*
			 * Iterator<String> i = sourceFiles.iterator(); while (i.hasNext())
			 * { String s = i.next(); ret.add(getOFile(s)); }
			 */
		}
		for (Module m : deps) {
			ret.addAll(m.getLinkableFiles());
		}

		return ret;
	}

	/* Mark a dependency as completed */
	public void markDependencyDone(Module m) {
		buildState.markDependencyDone(m);
	}

	/* Is to run each time a compile unit is done */
	public void markCompileUnitDone(CompileUnit unit) {
		buildState.markCompileUnitDone(unit);
	}

	/* Allows parent objects to check whether we're done or not */
	public boolean getDone() {
		return this.done;
	}

	public boolean toArchive() {
		return toArchive;
	}

	public boolean toLink() {
		return toLink;
	}

	public String archivedFile() {
		return this.archivedFile;
	}

	public String linkedFile() {
		return this.linkedFile;
	}

	public Module getParent() {
		return this.parent;
	}

	public String getLFile() {
		return Config.getInstance().getBuildDir() + "/" + linkedFile;
	}

	public String getAFile() {
		return Config.getInstance().getBuildDir() + "/" + archivedFile;
	}

	/* Turn the source file name into the name of a unique output file */
	public String getOFile(String inFile) {
		inFile = getCWD() + "/" + inFile;
		CLIDebug.getInstance().writeline("Target file: " + inFile);
		String mainDir = Config.getInstance().getBuildDir();
		int idx = 0;
		for (; idx < mainDir.length(); idx++) {
			if (!(mainDir.charAt(idx) == inFile.charAt(idx)))
				break;
		}

		inFile = inFile.substring(idx);
		inFile = inFile.substring(0, inFile.lastIndexOf("."));
		inFile = inFile.replace('\\', '_');
		inFile = inFile.replace('/', '-');

		return Config.getInstance().getBuildDir() + "/" + getName() + "-"
				+ inFile + ".o";
	}

	public void setDone() {
		this.done = true;
	}

	public String getCWD() {
		return this.cwd;
	}

	public ArrayList<String> getSourceFiles() {
		return this.sourceFiles;
	}

	public MakeModule getMakefile() {
		return this.makefile;
	}
}
