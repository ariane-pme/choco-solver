/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.explanations.strategies;


import solver.ICause;
import solver.explanations.BranchingDecision;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.RecorderExplanationEngine;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;

/**
 * A dynamic backtracking algorithm based on the decisio-repair, or path-repair principle.
 * It selects the decision to undo w.r.t. a {@link IDecisionJumper}.
 * Note that by giving {@link solver.explanations.strategies.jumper.MostRecentWorldJumper}, it acts like <code>dbt</code>.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/10/12
 */
public class PathRepair extends ConflictBasedBackjumping {

    DecisionsSet cobdec;

    public PathRepair(RecorderExplanationEngine mExplanationEngine, IDecisionJumper decisionJumper) {
        super(mExplanationEngine, decisionJumper);
        cobdec = new DecisionsSet(this);
    }

    @Override
    public void backtrackOn(Explanation explanation, ICause cause) {
        int upto = decisionJumper.compute(explanation, mSolver.getEnvironment().getWorldIndex());
        mSolver.getSearchLoop().overridePreviousWorld(upto);
        updateVRExplainUponbacktracking(upto, explanation, cause);
    }

    protected void updateVRExplainUponbacktracking(int nworld, Explanation expl, ICause cause) {
        if (cause == mSolver.getSearchLoop().getObjectivemanager()) {
            super.updateVRExplainUponbacktracking(nworld, expl, cause);
        }
        cobdec.clearDecisionPath();
        Decision dec = mSolver.getSearchLoop().decision; // the current decision to undo
        while (dec != RootDecision.ROOT && nworld > 1) {

            //TODO ajouter la validit�
            // 1. make a reverse copy of the decision, ready to be a LEFT branch
            if (!dec.hasNext()) {
                dec.reverse();
            }
            dec.rewind();
            // 3. add it to the pool of decision to force
            cobdec.push(dec);
            // get the previous
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != RootDecision.ROOT) {
            if (!dec.hasNext()) {
                throw new UnsupportedOperationException("PathRepair.updatVRExplain should get to a POSITIVE decision");
            }
            cobdec.setDecisionToRefute(dec);
            Deduction left = dec.getPositiveDeduction();
            expl.remove(left);
            assert left.getmType() == Deduction.Type.DecLeft;
            BranchingDecision va = (BranchingDecision) left;
            mExplanationEngine.removeLeftDecisionFrom(va.getDecision(), va.getVar());

            Deduction right = dec.getNegativeDeduction();
            mExplanationEngine.store(right, mExplanationEngine.flatten(expl));

            mSolver.getSearchLoop().decision = cobdec;
        }
        if (mExplanationEngine.isTraceOn() && LOGGER.isInfoEnabled()) {
            LOGGER.info("::EXPL:: BACKTRACK on " + dec /*+ " (up to " + nworld + " level(s))"*/);
        }
    }


}
