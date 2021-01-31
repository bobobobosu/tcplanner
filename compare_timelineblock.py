import json


with open("S:\\root\\Documents\\root\\Notebook\\TimelineBlockProblem.json","r",encoding="utf-8") as json_file:
    TimelineBlock_applied = json.load(json_file)
with open("S:\\root\\Documents\\root\\Notebook\\TimelineBlock.json","r",encoding="utf-8") as json_file:
    TimelineBlock_solution = json.load(json_file)


appliedlist = TimelineBlock_applied['timelineEntryList']
solutionlist = TimelineBlock_solution['timelineEntryList']

for idx in range(0,max(len(appliedlist),len(solutionlist))):
    if idx < len(appliedlist) and idx < len(solutionlist):
        # if appliedlist[idx]["progressChange"]["progressDelta"] != solutionlist[idx]["progressChange"]["progressDelta"]:
        #     print(str(appliedlist[idx]["progressChange"]["progressDelta"]) + '   ' + str(solutionlist[idx]["progressChange"]["progressDelta"]))
        if appliedlist[idx]["title"] != solutionlist[idx]["title"]:
            print(str(appliedlist[idx]["timelineProperty"]["rownum"]) + '   '+str(appliedlist[idx]["title"]) + '   ' + str(solutionlist[idx]["title"]))
        # pass
    else:
        if idx < len(appliedlist):
            applied = appliedlist[idx]
            print(str(appliedlist[idx]["timelineProperty"]["rownum"]) + '   '+str(appliedlist[idx]["title"]) )
        elif idx < len(solutionlist):
            solution = solutionlist[idx]
            print(str(solutionlist[idx]["timelineProperty"]["rownum"]) + '   '+str(solutionlist[idx]["title"]) )

        else:
            fff = 9
        fff = 9

# with open("TimelineBlock.json","r",encoding="utf-8") as json_file:
#     TimelineBlock = json.load(json_file)
# timelinelist = TimelineBlock['timelineEntryList']
# for idx in range(0,len(timelinelist)):
#     print(timelinelist[idx]['title'])


fff  =9 