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
package eu.orionos.build.generator;

import java.util.ArrayList;

import eu.orionos.build.Syntax;

public class ModuleGenerator {

	private ArrayList<Question> questions = new ArrayList<Question>();
	public ModuleGenerator()
	{
		questions.add(new Question(Syntax.MODULE_NAME, new AnswerString()));

		questions.add(new Question(Syntax.SOURCE, new AnswerArray<AnswerString>()));
		questions.add(new Question(Syntax.LINKED, new AnswerString()));
		questions.add(new Question(Syntax.ARCHIVED, new AnswerString()));

		questions.add(new Question(Syntax.LINK, new AnswerBoolean()));
		questions.add(new Question(Syntax.ARCHIVE, new AnswerBoolean()));

		questions.add(new Question(Syntax.GLOBAL_COMPILER, new AnswerString()));
		questions.add(new Question(Syntax.GLOBAL_LINKER, new AnswerString()));
		questions.add(new Question(Syntax.GLOBAL_ARCHIVER, new AnswerString()));

		questions.add(new Question(Syntax.GLOBAL_COMPILER_FLAGS, new AnswerString()));
		questions.add(new Question(Syntax.GLOBAL_LINKER_FLAGS, new AnswerString()));
		questions.add(new Question(Syntax.GLOBAL_ARCHIVER, new AnswerString()));

		questions.add(new Question(Syntax.DYN_COMPILER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));
		questions.add(new Question(Syntax.DYN_LINKER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));
		questions.add(new Question(Syntax.DYN_ARCHIVER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));

		questions.add(new Question(Syntax.DEP, new AnswerArray<AnswerObject<AnswerString>>()));
		questions.add(new Question(Syntax.DYN_DEP, new AnswerArray<AnswerObject<AnswerString>>()));

		questions.add(new Question(Syntax.MOD_COMPILER, new AnswerString()));
		questions.add(new Question(Syntax.MOD_LINKER, new AnswerString()));
		questions.add(new Question(Syntax.MOD_ARCHIVER, new AnswerString()));

		questions.add(new Question(Syntax.MOD_COMPILER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));
		questions.add(new Question(Syntax.MOD_LINKER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));
		questions.add(new Question(Syntax.MOD_ARCHIVER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));

		questions.add(new Question(Syntax.DYN_MOD_COMPILER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));
		questions.add(new Question(Syntax.DYN_MOD_LINKER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));
		questions.add(new Question(Syntax.DYN_MOD_ARCHIVER_FLAGS, new AnswerArray<AnswerObject<AnswerString>>()));

		Question[] question = questions.toArray(new Question[questions.size()]);
		for (Question q : question)
		{
			q.poseQuestion();
		}
	}

	void requestData()
	{

	}

	void writeJSON()
	{

	}
}
