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
<#--        <subSingleCount>1</subSingleCount>-->
    </inheritedSolverBenchmark>
<#--    <parallelBenchmarkCount>AUTO</parallelBenchmarkCount>-->


<#--    files-->
    <#list ['TimelineBlock'] as solution>
<#--    numbers-->
<#--    <#list [30000] as acceptedCountLimit>-->
    <#list [2147483646] as acceptedCountLimit>
    <#list [10000] as startingTemperature>
<#--    <#list ['0.3'] as etabuRatio>-->
    <#list [1] as lateAcceptanceSize>
<#--    algorithm-->
    <#list ['<lateAcceptanceSize>${lateAcceptanceSize}</lateAcceptanceSize>'] as lateAcceptance>
    <#list ['<simulatedAnnealingStartingTemperature>${startingTemperature}hard/${startingTemperature}medium/${startingTemperature}soft</simulatedAnnealingStartingTemperature>'] as simulatedAnnealing>
<#--    <#list ['<entityTabuRatio>0.02</entityTabuRatio>','<entityTabuRatio>0.002</entityTabuRatio>',''] as tabu>-->
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
    <#list ['<pickEarlyType>FIRST_BEST_SCORE_IMPROVING</pickEarlyType>'] as pickEarlyType>
<#--     <#list ['<selectionOrder>ORIGINAL</selectionOrder>',''] as selectionOrderCacheType>-->

<#--    <#list ['     <cacheType>PHASE</cacheType>-->
<#--                  <selectionOrder>SORTED</selectionOrder>-->
<#--                  <sorterManner>DECREASING_DIFFICULTY</sorterManner>',''] as entitySelector>-->
<#--    <#list ['${simulatedAnnealing}',''] as algorithm>-->
    <#list [''] as algorithm>
    <#list ['<constraintStreamImplType>BAVET</constraintStreamImplType>
             <constraintProviderClass>bo.tc.tcplanner.domain.score.ScheduleConstraintProvider</constraintProviderClass>'] as scoreProvider>
<#--    <#list ['<scoreDrl>${scoreDrl}</scoreDrl>'] as scoreProvider>-->
<#--    <#list [0.08] as delayWeight>-->
<#--    <#list ['${(1-delayWeight)/2}'?number] as progressWeight>-->
<#--    <#list ['${(1-delayWeight)/2}'?number] as timelineEntryWeight>-->

<#--    <#list [0.06,0.07,0.08,0.09,0.10] as progressWeight>-->
<#--    <#list ['${1-progressWeight}'?number] as startDateWeight>-->

<#--    <#list [0.10] as swapWeight>-->
<#--    <#list [0.30,0.35,0.40,0.45,0.50,0.55,0.60] as fineWeight>-->
<#--    <#list ['${1-swapWeight-fineWeight}'?number] as cartesianWeight>-->
<#--    <#list [0.02,0.04,0.06,0.08,0.10,0.12,0.14,0.16,0.18,0.20] as mergesplitWeight>-->
    <#list [0.11] as progressWeight>
    <#list ['${1-progressWeight}'?number] as startDateWeight>
    <#list [0.10] as swapWeight>
    <#list [0.35] as fineWeight>
    <#list ['${1-swapWeight-fineWeight}'?number] as cartesianWeight>
    <#list [0.04] as mergesplitWeight>

<#--    Filters-->
    <#list ['<filterClass>bo.tc.tcplanner.domain.filters.IsFocusedFilter</filterClass>'] as IsFocusedFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.filters.StartTimeCanChangeFilter</filterClass>'] as StartTimeCanChangeFilter>']
    <#list ['<filterClass>bo.tc.tcplanner.domain.filters.ProgressDeltaCanChangeFilter</filterClass>'] as ProgressDeltaCanChangeFilter>

<#--    Swap Moves-->
    <#list [' <cacheType>STEP</cacheType>
              <selectionOrder>PROBABILISTIC</selectionOrder>
              <probabilityWeightFactoryClass>bo.tc.tcplanner.domain.filters.AllocationProbabilityWeightFactory</probabilityWeightFactoryClass>'] as AllocationProbabilityWeight>
    <#list [
    '<swapMoveSelector>
            <fixedProbabilityWeight>${swapWeight}</fixedProbabilityWeight>
            <entitySelector id="entitySelector2">
                ${AllocationProbabilityWeight}
                ${StartTimeCanChangeFilter}
            </entitySelector>
            <secondaryEntitySelector>
                ${AllocationProbabilityWeight}
                 ${StartTimeCanChangeFilter}
            </secondaryEntitySelector>
            <variableNameInclude>planningStartDate</variableNameInclude>
        </swapMoveSelector>'] as swapMove>

<#--    cartesian Product Moves-->
    <#list ['<cartesianProductMoveSelector>
            <fixedProbabilityWeight>${(progressWeight+startDateWeight)*cartesianWeight}</fixedProbabilityWeight>
            <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
            <changeMoveSelector>
                <entitySelector id="cartesianSelector1">
                    ${AllocationProbabilityWeight}
                    ${StartTimeCanChangeFilter}
                </entitySelector>
                <valueSelector>
                    <variableName>planningStartDate</variableName>
                </valueSelector>
            </changeMoveSelector>
            <changeMoveSelector>
                <entitySelector mimicSelectorRef="cartesianSelector1"/>
                <valueSelector variableName="progressdelta"/>
            </changeMoveSelector>
        </cartesianProductMoveSelector>'] as cartesianMove>
    <#list ['<cartesianProductMoveSelector>
            <fixedProbabilityWeight>${(progressWeight+startDateWeight)*cartesianWeight}</fixedProbabilityWeight>
            <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
            <changeMoveSelector>
                <entitySelector>
                    ${AllocationProbabilityWeight}
                    ${StartTimeCanChangeFilter}
                </entitySelector>
                <valueSelector>
                    <variableName>planningStartDate</variableName>
                </valueSelector>
            </changeMoveSelector>
            <changeMoveSelector>
                <entitySelector>
                    ${AllocationProbabilityWeight}
                    ${StartTimeCanChangeFilter}
                </entitySelector>
                <valueSelector variableName="planningStartDate"/>
            </changeMoveSelector>
        </cartesianProductMoveSelector>'] as cartesianDoubleMove>

<#--        mergesplit-->
    <#list ['<moveListFactory>
            <fixedProbabilityWeight>${mergesplitWeight/2}</fixedProbabilityWeight>
            <moveListFactoryClass>bo.tc.tcplanner.domain.moves.SplitTimelineEntryFactory</moveListFactoryClass>
        </moveListFactory>'] as splitMove>
    <#list ['<moveListFactory>
            <fixedProbabilityWeight>${mergesplitWeight/2}</fixedProbabilityWeight>
            <moveListFactoryClass>bo.tc.tcplanner.domain.moves.MergeTimelineEntryMoveFactory</moveListFactoryClass>
        </moveListFactory>'] as mergeMove>

<#--    fine Moves-->
    <#list ['<moveIteratorFactory>
            <fixedProbabilityWeight>${startDateWeight*fineWeight/2}</fixedProbabilityWeight>
            <moveIteratorFactoryClass>bo.tc.tcplanner.domain.moves.PreciseStartDateMoveIteratorFactory</moveIteratorFactoryClass>
        </moveIteratorFactory>'] as PreciseStartDateMove>

    <#list ['<moveIteratorFactory>
            <fixedProbabilityWeight>${startDateWeight*fineWeight/32}</fixedProbabilityWeight>
            <moveIteratorFactoryClass>bo.tc.tcplanner.domain.moves.RemoveMoveIteratorFactory</moveIteratorFactoryClass>
        </moveIteratorFactory>'] as removeMove>

    <#list ['<changeMoveSelector>
                <fixedProbabilityWeight>${progressWeight*fineWeight}</fixedProbabilityWeight>
                <entitySelector>
                    ${AllocationProbabilityWeight}
                    ${IsFocusedFilter}
                    ${ProgressDeltaCanChangeFilter}
                </entitySelector>
                <valueSelector variableName="progressdelta"/>
            </changeMoveSelector>'] as progressdelta>

    <#list ['<changeMoveSelector>
            <fixedProbabilityWeight>${startDateWeight*fineWeight/2}</fixedProbabilityWeight>
                <entitySelector>
                    ${AllocationProbabilityWeight}
                </entitySelector>
            <valueSelector variableName="planningStartDate"/>
        </changeMoveSelector>'] as planningStartDate>

    <#list ['
            ${swapMove}
            ${cartesianMove}
            ${splitMove}
            ${mergeMove}
            ${removeMove}
            ${planningStartDate}
            ${progressdelta}'] as customMoves>
<#--        <#list ['-->
<#--            ${planningStartDate}-->
<#--            ${progressdelta}'] as customMoves>-->

    <#list ['<constructionHeuristic>
        <queuedEntityPlacer>
            <entitySelector id="placerEntitySelector">
                <cacheType>PHASE</cacheType>
                <selectionOrder>SORTED</selectionOrder>
                <sorterManner>DECREASING_DIFFICULTY</sorterManner>
                ${StartTimeCanChangeFilter}
            </entitySelector>
            <changeMoveSelector>
                <entitySelector mimicSelectorRef="placerEntitySelector"/>
                <valueSelector variableName="planningStartDate"/>
            </changeMoveSelector>
            <changeMoveSelector>
                <entitySelector mimicSelectorRef="placerEntitySelector"/>
                <valueSelector variableName="progressdelta"/>
            </changeMoveSelector>
        </queuedEntityPlacer>
    </constructionHeuristic>'] as constructionHeuristic1>


    <solverBenchmark>
        <name>a${acceptedCountLimit?index}b${fineWeight?index}c${removeMove?index}</name>
        <problemBenchmarks>
            <inputSolutionFile>./src/main/resources/Solutions/${solution}.json</inputSolutionFile>
        </problemBenchmarks>
        <solver>
            <environmentMode>${envmode}</environmentMode>
            <scoreDirectorFactory>
                ${scoreProvider}
            </scoreDirectorFactory>
<#--            ${constructionHeuristic1}-->
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
                        ${pickEarlyType}
                </forager>
                <termination>
                    <bestScoreLimit>0hard/-2147483648medium/-2147483648soft</bestScoreLimit>
<#--                                    <unimprovedSecondsSpentLimit>30</unimprovedSecondsSpentLimit>-->
                    <millisecondsSpentLimit>150000</millisecondsSpentLimit>
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
</plannerBenchmark>