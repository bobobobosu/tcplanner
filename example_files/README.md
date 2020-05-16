# JSON Schema for input files
## Table of Contents

* [TimeHierarchyMap.json](#timehierarchymap)
* [LocationHierarchyMap.json](locationhierarchymap)
* [ValueEntryMap.json](#valueentrymap)
* [TimelineBlock.json](#timelineblock)

## TimeHierarchyMap.json
Supported time strings:
* iso8601 ("2019-09-23T00:00:00-07:00/2019-12-14T00:00:00-07:00")
* weekday (1, 2, 3, 4, 5, 6, 7)
* time ("11:00:00/13:00:00" specifies an interval)
```
{
  "Fall Quarter": [                     // Name of the time definition
    [       // List of time constraints (the result is the intersection of 
            // all elements in this list)
      {
        "iso8601": [                    // Declare format for time string
            // List of time strings (element in the list are "or"ed together)
          "2019-09-23T00:00:00-07:00/2019-12-14T00:00:00-07:00"  
        ]
      }
    ]
  ], 
  "Monday": [
    [
      {
        "weekday": [
          1
        ]
      }
    ]
  ],
  "Noon": [
    [
      {
        "time": [
          "11:00:00/13:00:00"
        ]
      }
    ]
  ]
}
```


## LocationHierarchyMap.json
Parent string is the location name.
Child list contains location strings that can be implied by the parent location.
For example, being at "Sunnyvale" means you are in California and U.S..
```
{
  "": [
    "", 
    "LocationHierarchyMap"
  ], 
  "Sunnyvale": [
    "Sunnyvale", 
    "California", 
    "U.S.", 
    "Undefined", 
    "", 
    "LocationHierarchyMap"
  ], 
  "Taiwan Taoyuan International Airport": [
    "Taiwan Taoyuan International Airport", 
    "Taiwan", 
    "Undefined", 
    "", 
    "LocationHierarchyMap"
  ], 
}
```

## ValueEntryMap.json
ValueEntryMap contains properties of tasks and resources. The planner would add/remove
tasks to the plan by creating tasks according to the prototypes defined in this list. 
```
{
  "Sleep": {                                // task or resource name
    "wbs": 0,                               // id for work breakdown schedule(unused)
    "type": "task",                         // can be "task", "resource", or "project"
    "classification": "*",                  // only tasks classified as * will be used
    "capacity": 1.0,                        // max amount of this resouce can exist at a time
                                            // (unused if type is "task")
    "humanStateChangeList": [
      {
        "currentLocation": "Home",          // required location in which this task can perform
        "movetoLocation": "Undefined",      // location after performing this task ("undefined" if no change)
        "duration": 476.24999999999284,     // duration in minutes
        "requirementTimerange": "Anytime",  // time window in which this task must perform
        "adviceTimerange": "Sleep"          // preferred time window for this task 
      }
    ],
    "resourceStateChangeList": [            // list of resource changes after performing this task
      {
        "mode": "delta",                    // "delta" if the amt in resourceChanges is delta from 
                                            // previous state. "absolute" will overwrite the current resource
                                            // amount
        "resourceChange": {
        }
      }
    ],
    "progressChangeList": [
      {
        "progressDelta": 1,                 // progress percentage of this task (should between 0 and 1)
                                            // beware that duration and resource change amount directly 
                                            // links to this value
        "progressPreset": [                 // unused
          {
            "percentage": 1.0,
            "milestone": "",
            "task": "",
            "taskCount": 1.0,
            "upMilestone": "End"
          }
        ],
        "progressLog": []                   // unused
      }
    ],
    "chronoProperty": {
      "draggable": 1,                       // 1 if this task can be moved around (not fixed at a time)
      "substitutable": 1,                   // 1 if this task can be freely removed from draft 
      "splittable": 0.0,                    // 1 if this task can be divided into portions 
                                            // (each progressDelta sums to original progressDelta)
      "gravity": 0.0                        // -1 if this task is scheduled earlier the better,
                                            // 0 if this task is neutral
                                            // 1 if this task is scheduled later the better
    }
  }
}
```

## TimelineBlock.json
```
{
  "timelineEntryList": [                    // list of timeline entries, unsorted
    {
      "title": "Sleep",                     // title of this entry
      "description": "",                    // description of this entry
      "humanStateChange": {
        "currentLocation": "Home",          // required location in which this task can perform
        "movetoLocation": "Undefined",      // location after performing this task ("undefined" if no change)
        "duration": 471.99999999999886,     // duration in minutes
        "requirementTimerange": "Anytime",  // time window in which this task must perform
        "adviceTimerange": "Sleep"          // preferred time window for this task 
      },
      "resourceStateChange": {
        "resourceChange": {                 // list of resource changes after performing this task
          "r.Calories Consumption": [
            {
              "amt": -1.0,
              "location": "Anywhere"
            }
          ]
        },
        "mode": "delta"                     // "delta" if the amt in resourceChanges is delta from 
                                            // previous state. "absolute" will overwrite the current resource
                                            // amount
      },
      "progressChange": {
        "progressDelta": 1.0,               // progress percentage of this task (should between 0 and 1)
                                            // beware that duration and resource change amount directly 
                                            // links to this value
        "progressPreset": [                 // unused
          {
            "percentage": 0.112629629625007,
            "milestone": "Prepare",
            "task": "Prepare",
            "taskCount": 1.0,
            "upMilestone": "End"
          },
          {
            "percentage": 0.95,
            "milestone": "Sleep",
            "task": "Sleep",
            "taskCount": 1.0,
            "upMilestone": "End"
          },
          {
            "percentage": 1.0,
            "milestone": "Wakeup",
            "task": "Wakeup",
            "taskCount": 1.0,
            "upMilestone": "End"
          }
        ],
        "progressLog": []
      },
      "chronoProperty": {
        "startTime": "2020-06-13T01:00:00+08:00[Asia/Taipei]",  // initial planned start time
        "deadline": "2020-06-13T08:52:00+08:00[Asia/Taipei]",   // task must complete before deadline
        "aliveline": "2020-05-08T12:00:00+08:00[Asia/Taipei]",  // task must start after aliveline
        "priority": 1.0,                                        // larger number, higher priority 
        "draggable": 0.0,                                       // 1 if this task can be moved around (not fixed at a time)              
        "substitutable": 0.0,                                   // 1 if this task can be freely removed from draft 
        "splittable": 0.0,                                      // 1 if this task can be divided into portions 
        "gravity": 0.0                                          // -1 if this task is scheduled earlier the better,
                                                                // 0 if this task is neutral
                                                                // 1 if this task is scheduled later the better
      },
      "timelineProperty": {
        "timelineid": 1180,                                     // external id (useful when updating external database)
        "rownum": 471.0,                                        // external rownum (useful when updating external database)
        "planningWindowType": "Published",                      // "Draft" if with this task to be reschedule freely
                                                                // "Published" if preferred not to move this task (no guarantee)
                                                                // "History" if this task must not change at all
        "dependencyIdList": [],                                 // list of task dependencies (must be completed before this task)
        "taskChainIdList": [                                    // list of tasks that is counted as the same task with this task
                                                                // (just being splitted into multiple sub-tasks)
          1180
        ]
      }
    }
  ],
  "blockStartTime": "2020-05-08T12:00:00+08:00[Asia/Taipei]",   // global start date time of this timeline block
  "blockEndTime": "2020-06-13T08:52:00+08:00[Asia/Taipei]",     // global end date time of this timeline block
  "blockScheduleAfter": "2020-05-14T17:53:57+08:00[Asia/Taipei]",   // any task that is before this date will be marked as "History"
  "score": "",
  "origin": "TCxlsb"
}
```