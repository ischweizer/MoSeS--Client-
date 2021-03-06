/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.da_sense.moses.client.userstudy;

import java.util.List;


/**
 * This class represents a single Question. A question is contained in an
 * instance of of a {@link Form}. Depending of its type, a question may contains a {@link List} of {@link PossibleAnswer}
 * instances. The question types that contain possible answers are
 * {@link Question#TYPE_MULTIPLE_CHOICE} and {@link Question#TYPE_SINGLE_CHOICE}.
 */
public class Question extends HasID implements IHasTitle{
	
	//=================QUESTION TYPES===========================
	public static final int TYPE_YES_NO_QUESTION = 1;
	public static final int TYPE_TEXT_QUESTION = 2;
	public static final int TYPE_LIKERT_SCALE = 3;
	public static final int TYPE_MULTIPLE_CHOICE = 4;
	public static final int TYPE_SINGLE_CHOICE = 5;
	//=============END QUESTION TYPES END=======================
	
	//=================MANDATORY OR NOT===========================
	public static final int MANDATORY_QUESTION_NO = 0;
	public static final int MANDATORY_QUESTION_YES = 1;
	//=============END MANDATORY OR NOT END=======================
	
	
	/**
	 * A constant of describing an "unanswered" answer to a question.
	 * @see <a href="https://github.com/ischweizer/MoSeS/wiki/Question-types">Answer codings</a>
	 */
	public static final String ANSWER_UNANSWERED = "";
	
	private int mType;
	
	private String mTitle;
	
	private int mIsMandatory = MANDATORY_QUESTION_NO;
	
	/**
	 * An answer to this question, given by the user. The answer to a question
	 * depends on question's type.
	 * @see <a href="https://github.com/ischweizer/MoSeS/wiki/Question-types">Answer codings</a>
	 */
	private String mAnswer = ANSWER_UNANSWERED;
	
	/**
	 * Sets the type of this question.
	 * @param mType the type to set
	 */
	public void setType(int type) {
		this.mType = type;
	}

	private List<PossibleAnswer> mPossibleAnswers;
	
	/**
	 * Returns the type of the Question.
	 * 
	 * @return type
	 */
	public int getType() {
		return mType;
	}

	@Override
	public String setTitle(String title) {
		String oldTitle = mTitle;
		mTitle = title;
		return oldTitle;
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Returns all {@link PossibleAnswer} instances attached to this question.
	 * @return all possible answers of this question, if the question does not
	 * have any possible question, the method returns null
	 */
	public List<PossibleAnswer> getPossibleAnswers() {
		return mPossibleAnswers;
	}

	/**
	 * Sets the list of {@link PossibleAnswer} instances to this question.
	 * @param possibleAnswers the answers to set
	 */
	public void setPossibleAnswers(List<PossibleAnswer> possibleAnswers) {
		this.mPossibleAnswers = possibleAnswers;
	}

	/**
	 * Returns the answer the user gave to this question if any.
	 * @return {@link String} containing the answer to this question.
	 */
	public String getAnswer() {
		return mAnswer;
	}
	
	/**
	 * Sets the answer to this question.
	 * @param answer the answer to set
	 */
	public void setAnswer(String answer){
		this.mAnswer = answer;
	}

	/**
	 * Returns {@link Question#MANDATORY_QUESTION_YES} if and only if this question is mandatory.
	 * @return {@link Question#MANDATORY_QUESTION_YES} if the question is mandatory, {@link Question#MANDATORY_QUESTION_NO} otherwise.
	 */
	public int getIsMandatory() {
		return mIsMandatory;
	}

	/**
	 * Sets the flag to this {@link Question} instance signaling if the question is mandatory or not.
	 * @param isMandatory set {@link Question#MANDATORY_QUESTION_NO} if the question is not mandatory, {@link Question#MANDATORY_QUESTION_YES}
	 * otherwise.
	 */
	public void setIsMandatory(int isMandatory) {
		this.mIsMandatory = isMandatory;
	}
	
	/**
	 * Returns true if and only if this question is mandatory.
	 * @return true if this question is mandatory, false otherwise.
	 */
	public boolean isMandatory(){
		return getIsMandatory() == MANDATORY_QUESTION_YES;
	}
}
