<?xml version="1.0" encoding="UTF-8"?>
<plannerBenchmark>
    <benchmarkDirectory>benchmark/tcplannercore</benchmarkDirectory>
<#--    <parallelBenchmarkCount>AUTO</parallelBenchmarkCount>-->
    <inheritedSolverBenchmark>
        <problemBenchmarks>
            <solutionFileIOClass>bo.tc.tcplanner.datastructure.persistence.TimelineBlockScheduleFileIO</solutionFileIOClass>
            <problemStatisticType>BEST_SCORE</problemStatisticType>
            <problemStatisticType>STEP_SCORE</problemStatisticType>
            <problemStatisticType>MEMORY_USE</problemStatisticType>
            <problemStatisticType>CALCULATE_COUNT_PER_SECOND</problemStatisticType>
            <singleStatisticType>CONSTRAINT_MATCH_TOTAL_BEST_SCORE</singleStatisticType>
            <singleStatisticType>PICKED_MOVE_TYPE_BEST_SCORE_DIFF</singleStatisticType>
        </problemBenchmarks>

        <solver>
            <moveThreadCount>AUTO</moveThreadCount>
<#--            <solutionClass>Schedule</solutionClass>-->
<#--            <entityClass>Allocation</entityClass>-->
            <scanAnnotatedClasses>
                <packageInclude>bo.tc.tcplanner</packageInclude>
            </scanAnnotatedClasses>
<#--            <termination>-->
<#--                <millisecondsSpentLimit>10</millisecondsSpentLimit>-->
<#--            </termination>-->
        </solver>

    </inheritedSolverBenchmark>


<#--    files-->
    <#list ['TimelineBlockProblem'] as solution>
<#--    numbers-->
    <#list [1500] as acceptedCountLimit>
    <#list [10000] as startingTemperature>
<#--    <#list ['0.3'] as etabuRatio>-->
    <#list [1] as lateAcceptanceSize>
<#--    algorithm-->
    <#list ['<lateAcceptanceSize>${lateAcceptanceSize}</lateAcceptanceSize>'] as lateAcceptance>
    <#list ['<simulatedAnnealingStartingTemperature>[${startingTemperature}/${startingTemperature}/${startingTemperature}/${startingTemperature}/${startingTemperature}]hard/[0/0/0/0]soft</simulatedAnnealingStartingTemperature>'] as simulatedAnnealing>
    <#list ['<entityTabuRatio>0.002</entityTabuRatio>'] as tabu>
    <#list ['<valueTabuRatio>0.002</valueTabuRatio>'] as vtabu>
    <#list ['<moveTabuSize>3</moveTabuSize>'] as mtabu>
    <#list ['<undoMoveTabuSize>5</undoMoveTabuSize>'] as umtabu>
    <#list ['REPRODUCIBLE'] as envmode>
    <#list ['TCRules_P1_0.drl'] as scoreDrl>
    <#list ['<constructionHeuristic>
                 <constructionHeuristicType>FIRST_FIT</constructionHeuristicType>
             </constructionHeuristic>'] as constructionHeuristic>
<#--    <#list [''] as finalistPodiumType>-->
    <#list ['<finalistPodiumType>STRATEGIC_OSCILLATION_BY_LEVEL</finalistPodiumType>'] as finalistPodiumType>
    <#list ['FIRST_BEST_SCORE_IMPROVING'] as pickEarlyType>
<#--     <#list ['<selectionOrder>ORIGINAL</selectionOrder>',''] as selectionOrderCacheType>-->

<#--    <#list ['     <cacheType>PHASE</cacheType>-->
<#--                  <selectionOrder>SORTED</selectionOrder>-->
<#--                  <sorterManner>DECREASING_DIFFICULTY</sorterManner>',''] as entitySelector>-->
<#--    <#list ['','${simulatedAnnealing}'] as algorithm>-->
    <#list [''] as algorithm>
    <#list ['<constraintStreamImplType>BAVET</constraintStreamImplType>
             <constraintProviderClass>ScheduleConstraintProvider</constraintProviderClass>'] as scoreProvider>
<#--    <#list ['<scoreDrl>${scoreDrl}</scoreDrl>'] as scoreProvider>-->
<#--    <#list [0.08] as delayWeight>-->
<#--    <#list ['${(1-delayWeight)/2}'?number] as progressWeight>-->
<#--    <#list ['${(1-delayWeight)/2}'?number] as timelineEntryWeight>-->
    <#list [0.08] as delayWeight>
    <#list [0.1] as progressWeight>
    <#list ['${(1-progressWeight)}'?number] as timelineEntryWeight>


    <#list [0.10] as swapWeight>
    <#list [0.10] as fineWeightRest>
    <#list [0.80] as cartesianWeightRest>

    <#list ['${(1-swapWeight)*fineWeightRest}'?number] as fineWeight>
    <#list ['${(1-swapWeight)*(1-fineWeightRest)*cartesianWeightRest}'?number] as cartesianWeight>
    <#list ['${(1-swapWeight)*(1-fineWeightRest)*(1-cartesianWeightRest)}'?number] as mergesplitWeight>

<#--    Moves-->
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.CondensedDummyAllocationFilter</filterClass>'] as CondensedDummyAllocationFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.CondensedAllocationFilter</filterClass>'] as CondensedAllocationFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.IsFocusedFilter</filterClass>'] as IsFocusedFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.DelayCanChangeFilter</filterClass>'] as DelayCanChangeFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.TimelineEntryCanChangeFilter</filterClass>${CondensedAllocationFilter}'] as TimelineEntryCanChangeFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.ProgressDeltaCanChangeFilter</filterClass>'] as ProgressDeltaCanChangeFilter>


    <#list [' <cacheType>STEP</cacheType>
              <selectionOrder>PROBABILISTIC</selectionOrder>
              <probabilityWeightFactoryClass>AllocationProbabilityWeightFactory</probabilityWeightFactoryClass>'] as AllocationProbabilityWeight>
<#--    <#list [''] as AllocationProbabilityWeight>-->
    <#list [
            '<swapMoveSelector>
                <fixedProbabilityWeight>${swapWeight}</fixedProbabilityWeight>
                <entitySelector id="entitySelector2">
                    ${IsFocusedFilter}
                    ${TimelineEntryCanChangeFilter}
                </entitySelector>
                <secondaryEntitySelector>
                     ${AllocationProbabilityWeight}
                     ${TimelineEntryCanChangeFilter}
                </secondaryEntitySelector>
                <variableNameInclude>timelineEntry</variableNameInclude>
                <variableNameInclude>progressdelta</variableNameInclude>
            </swapMoveSelector>'] as swapMove>

<#--    cartesian Product Moves-->
    <#list ['<cartesianProductMoveSelector>
                <fixedProbabilityWeight>${(progressWeight+timelineEntryWeight)*cartesianWeight}</fixedProbabilityWeight>
                <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
                <changeMoveSelector>
                    <entitySelector id="cartesianSelector1">
                        ${AllocationProbabilityWeight}
                        ${TimelineEntryCanChangeFilter}
                    </entitySelector>
                    <valueSelector>
                        <variableName>timelineEntry</variableName>
                    </valueSelector>
                </changeMoveSelector>
                <changeMoveSelector>
                    <entitySelector mimicSelectorRef="cartesianSelector1"/>
                    <valueSelector variableName="progressdelta"/>
                </changeMoveSelector>
            </cartesianProductMoveSelector>'] as cartesiantimelineEntry>
    <#list ['<cartesianProductMoveSelector>
                <fixedProbabilityWeight>${delayWeight*cartesianWeight}</fixedProbabilityWeight>
                <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
                <changeMoveSelector>
                    <entitySelector>
                        ${IsFocusedFilter}
                        ${DelayCanChangeFilter}
                    </entitySelector>
                    <valueSelector variableName="delay"/>
                </changeMoveSelector>
                <changeMoveSelector>
                    <entitySelector>
                        ${IsFocusedFilter}
                        ${DelayCanChangeFilter}
                    </entitySelector>
                    <valueSelector variableName="delay"/>
                </changeMoveSelector>
            </cartesianProductMoveSelector>'] as cartesiandelay>


<#--    fine Moves-->
    <#list ['<moveIteratorFactory>
                <fixedProbabilityWeight>${timelineEntryWeight*fineWeight}</fixedProbabilityWeight>
                <moveIteratorFactoryClass>bo.tc.tcplanner.domain.solver.moves.PreciseTimeEntryMoveIteratorFactory</moveIteratorFactoryClass>
            </moveIteratorFactory>'] as timelineEntryPrecise>

    <#list ['<changeMoveSelector>
                    <fixedProbabilityWeight>${timelineEntryWeight*fineWeight}</fixedProbabilityWeight>
                    <entitySelector id="changeSelector1">
                        ${AllocationProbabilityWeight}
                        ${TimelineEntryCanChangeFilter}
                    </entitySelector>
                    <valueSelector>
                        <variableName>timelineEntry</variableName>
                    </valueSelector>
            </changeMoveSelector>'] as timelineEntry>

    <#list ['<changeMoveSelector>
                <fixedProbabilityWeight>${progressWeight*fineWeight}</fixedProbabilityWeight>
                <entitySelector>
                    ${IsFocusedFilter}
                    ${ProgressDeltaCanChangeFilter}
                </entitySelector>
                <valueSelector variableName="progressdelta"/>
            </changeMoveSelector>'] as progressdelta>

    <#list ['<changeMoveSelector>
            <fixedProbabilityWeight>${delayWeight*fineWeight/2}</fixedProbabilityWeight>
            <entitySelector>
                ${IsFocusedFilter}
                ${DelayCanChangeFilter}
            </entitySelector>
            <valueSelector variableName="planningStartDate"/>
        </changeMoveSelector>'] as planningStartDate>

    <#list ['<changeMoveSelector>
                <fixedProbabilityWeight>${delayWeight*fineWeight/2}</fixedProbabilityWeight>
                <entitySelector>
                    ${IsFocusedFilter}
                    ${DelayCanChangeFilter}
                </entitySelector>
                <valueSelector variableName="delay"/>
            </changeMoveSelector>'] as delay>
    <#list ['<moveIteratorFactory>
                <fixedProbabilityWeight>${delayWeight*fineWeight/2}</fixedProbabilityWeight>
                <moveIteratorFactoryClass>PreciseStartDateMoveIteratorFactory</moveIteratorFactoryClass>
            </moveIteratorFactory>'] as precisedelay>

<#--        mergesplit-->
    <#list ['<moveListFactory>
                <fixedProbabilityWeight>${mergesplitWeight/2}</fixedProbabilityWeight>
                <moveListFactoryClass>SplitTimelineEntryFactory</moveListFactoryClass>
            </moveListFactory>'] as splitMove>
    <#list ['<moveListFactory>
                <fixedProbabilityWeight>${mergesplitWeight/2}</fixedProbabilityWeight>
                <moveListFactoryClass>MergeTimelineEntryMoveFactory</moveListFactoryClass>
            </moveListFactory>'] as mergeMove>

    <#list ['${progressdelta}
            ${timelineEntry}
            ${precisedelay}
            ${delay}'] as fineMoves>
    <#list ['${cartesiantimelineEntry}
             ${cartesiandelay}'] as cartesianMoves>
    <#list ['${mergeMove}
            ${splitMove}'] as mergesplitMoves>

    <#list ['
            ${swapMove}
            ${progressdelta}
            ${timelineEntry}
            ${timelineEntryPrecise}
            ${precisedelay}
            ${delay}
            ${cartesiantimelineEntry}
            ${cartesiandelay}
            ${mergeMove}
            ${splitMove}'] as customMoves>
    <#list ['<constructionHeuristic>
                <queuedEntityPlacer>
                    <entitySelector id="placerEntitySelector2">
                        <cacheType>PHASE</cacheType>
                        <selectionOrder>SORTED</selectionOrder>
                        <sorterManner>DECREASING_DIFFICULTY</sorterManner>
                        ${DelayCanChangeFilter}
                    </entitySelector>
                    <changeMoveSelector>
                        <entitySelector mimicSelectorRef="placerEntitySelector2"/>
                        <valueSelector variableName="delay"/>
                    </changeMoveSelector>
                </queuedEntityPlacer>
            </constructionHeuristic>'] as constructionHeuristic1>
    <#list ['<constructionHeuristic>
                <queuedEntityPlacer>
                    <entitySelector id="placerEntitySelector">
                        <cacheType>PHASE</cacheType>
                        <selectionOrder>SORTED</selectionOrder>
                        <sorterManner>DECREASING_DIFFICULTY</sorterManner>
                        ${TimelineEntryCanChangeFilter}
                    </entitySelector>
                    <changeMoveSelector>
                        <entitySelector mimicSelectorRef="placerEntitySelector"/>
                        <valueSelector variableName="timelineEntry"/>
                    </changeMoveSelector>
                    <changeMoveSelector>
                        <entitySelector mimicSelectorRef="placerEntitySelector"/>
                        <valueSelector variableName="progressdelta"/>
                    </changeMoveSelector>
                </queuedEntityPlacer>
            </constructionHeuristic>'] as constructionHeuristic2>
<#--        <termination>-->
<#--            <millisecondsSpentLimit>1800</millisecondsSpentLimit>-->
<#--        </termination>-->

    <solverBenchmark>
        <name>a${constructionHeuristic?index}b${timelineEntryPrecise?index}c${swapMove?index}d${delayWeight?index}</name>
        <problemBenchmarks>
            <inputSolutionFile>S:/root/Code/tcplannercore/src/main/resources/Solutions/${solution}.json</inputSolutionFile>
        </problemBenchmarks>
        <solver>
            <environmentMode>${envmode}</environmentMode>
            <scoreDirectorFactory>
                ${scoreProvider}
            </scoreDirectorFactory>
<#--            ${constructionHeuristic1}-->
<#--            ${constructionHeuristic2}-->
            <localSearch>
                <unionMoveSelector>
                    ${customMoves}
                </unionMoveSelector>
                <#if lateAcceptance != "" || tabu != "" || mtabu != "" || umtabu != "">
                    <acceptor>
                            ${algorithm}
<#--                        ${lateAcceptance}-->
<#--                        ${vtabu}-->
<#--                        ${tabu}-->
                        ${mtabu}
                        ${umtabu}
                    </acceptor>
                </#if>
                <forager>
<#--                    <acceptedCountLimit>4</acceptedCountLimit>-->
                        <acceptedCountLimit>${acceptedCountLimit}</acceptedCountLimit>
                        ${finalistPodiumType}
                        <pickEarlyType>${pickEarlyType}</pickEarlyType>
                </forager>
                <termination>
                    <bestScoreLimit>0hard/-2147483648medium/-2147483648soft</bestScoreLimit>
<#--                                    <unimprovedSecondsSpentLimit>50</unimprovedSecondsSpentLimit>-->
                    <millisecondsSpentLimit>30000</millisecondsSpentLimit>
                </termination>
            </localSearch>
        </solver>
    </solverBenchmark>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
</plannerBenchmark>