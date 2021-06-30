<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Clock

Non-visible component that provides the instant in time using the internal clock on the phone. It can fire a timer at regularly set intervals and perform time calculations, manipulations, and conversions.

Methods to convert an instant to text are also available. Acceptable patterns are empty string, MM/DD/YYYY HH:mm:ss a, or MMM d, yyyyHH:mm. The empty string will provide the default format, which is "MMM d, yyyy HH:mm:ss a" for FormatDateTime "MMM d, yyyy" for FormatDate. To see all possible format, please see [here](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html).

---

## Designer Properties

---

### TimerAlwaysFires

|   Property Name  | Editor Type | Default Value |
| :--------------: | :---------: | :-----------: |
| TimerAlwaysFires |   boolean   |      True     |

### TimerEnabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  TimerEnabled |   boolean   |      True     |

### TimerInterval

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
| TimerInterval | non_negative_integer |      1000     |

## Events

---

### Timer

<div block-type = "component_event" component-selector = "Clock" event-selector = "Timer" id = "clock-timer"></div>

The Timer event runs when the timer has gone off.

## Methods

---

### AddDays

<div block-type = "component_method" component-selector = "Clock" method-selector = "AddDays" id = "clock-adddays"></div>

Return Type : InstantInTime

Returns an instant in time some days after the given instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|  quantity  |     number    |

### AddDuration

<div block-type = "component_method" component-selector = "Clock" method-selector = "AddDuration" id = "clock-addduration"></div>

Return Type : InstantInTime

Returns an instant in time some duration after the argument

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|  quantity  |     number    |

### AddHours

<div block-type = "component_method" component-selector = "Clock" method-selector = "AddHours" id = "clock-addhours"></div>

Return Type : InstantInTime

Returns an instant in time some hours after the given instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|  quantity  |     number    |

### AddMinutes

<div block-type = "component_method" component-selector = "Clock" method-selector = "AddMinutes" id = "clock-addminutes"></div>

Return Type : InstantInTime

Returns an instant in time some minutes after the given instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|  quantity  |     number    |

### AddMonths

<div block-type = "component_method" component-selector = "Clock" method-selector = "AddMonths" id = "clock-addmonths"></div>

Return Type : InstantInTime

Returns an instant in time some months after the given instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|  quantity  |     number    |

### AddSeconds

<div block-type = "component_method" component-selector = "Clock" method-selector = "AddSeconds" id = "clock-addseconds"></div>

Return Type : InstantInTime

Returns an instant in time some seconds after the given instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|  quantity  |     number    |

### AddWeeks

<div block-type = "component_method" component-selector = "Clock" method-selector = "AddWeeks" id = "clock-addweeks"></div>

Return Type : InstantInTime

Returns An instant in time some weeks after the given instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|  quantity  |     number    |

### AddYears

<div block-type = "component_method" component-selector = "Clock" method-selector = "AddYears" id = "clock-addyears"></div>

Return Type : InstantInTime

Returns an instant in time some years after the given instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|  quantity  |     number    |

### DayOfMonth

<div block-type = "component_method" component-selector = "Clock" method-selector = "DayOfMonth" id = "clock-dayofmonth"></div>

Return Type : number

Returns the day of the month (1-31) from the instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### Duration

<div block-type = "component_method" component-selector = "Clock" method-selector = "Duration" id = "clock-duration"></div>

Return Type : number

Returns duration, which is milliseconds elapsed between instants.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|    start   | InstantInTime |
|     end    | InstantInTime |

### DurationToDays

<div block-type = "component_method" component-selector = "Clock" method-selector = "DurationToDays" id = "clock-durationtodays"></div>

Return Type : number

Converts the duration to the number of days.

| Param Name | Input Type |
| :--------: | :--------: |
|  duration  |   number   |

### DurationToHours

<div block-type = "component_method" component-selector = "Clock" method-selector = "DurationToHours" id = "clock-durationtohours"></div>

Return Type : number

Converts the duration to the number of hours.

| Param Name | Input Type |
| :--------: | :--------: |
|  duration  |   number   |

### DurationToMinutes

<div block-type = "component_method" component-selector = "Clock" method-selector = "DurationToMinutes" id = "clock-durationtominutes"></div>

Return Type : number

Converts the duration to the number of minutes.

| Param Name | Input Type |
| :--------: | :--------: |
|  duration  |   number   |

### DurationToSeconds

<div block-type = "component_method" component-selector = "Clock" method-selector = "DurationToSeconds" id = "clock-durationtoseconds"></div>

Return Type : number

Converts the duration to the number of seconds.

| Param Name | Input Type |
| :--------: | :--------: |
|  duration  |   number   |

### DurationToWeeks

<div block-type = "component_method" component-selector = "Clock" method-selector = "DurationToWeeks" id = "clock-durationtoweeks"></div>

Return Type : number

Converts the duration to the number of weeks.

| Param Name | Input Type |
| :--------: | :--------: |
|  duration  |   number   |

### FormatDate

<div block-type = "component_method" component-selector = "Clock" method-selector = "FormatDate" id = "clock-formatdate"></div>

Return Type : text

Text representing the date of an instant in the specified pattern

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|   pattern  |      text     |

### FormatDateTime

<div block-type = "component_method" component-selector = "Clock" method-selector = "FormatDateTime" id = "clock-formatdatetime"></div>

Return Type : text

Returns text representing the date and time of an instant in the specified pattern

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |
|   pattern  |      text     |

### FormatTime

<div block-type = "component_method" component-selector = "Clock" method-selector = "FormatTime" id = "clock-formattime"></div>

Return Type : text

Text representing the time of an instant

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### GetMillis

<div block-type = "component_method" component-selector = "Clock" method-selector = "GetMillis" id = "clock-getmillis"></div>

Return Type : number

Returns the instant in time measured as milliseconds since 1970.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### Hour

<div block-type = "component_method" component-selector = "Clock" method-selector = "Hour" id = "clock-hour"></div>

Return Type : number

Returns the hour of the day (0-23) from the instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### MakeDate

<div block-type = "component_method" component-selector = "Clock" method-selector = "MakeDate" id = "clock-makedate"></div>

Return Type : InstantInTime

Returns an instant in time specified by year, month, date in UTC. Valid values for the month field are 1-12 and 1-31 for the day field.

| Param Name | Input Type |
| :--------: | :--------: |
|    year    |   number   |
|    month   |   number   |
|     day    |   number   |

### MakeInstant

<div block-type = "component_method" component-selector = "Clock" method-selector = "MakeInstant" id = "clock-makeinstant"></div>

Return Type : InstantInTime

Returns an instant in time specified by MM/dd/YYYY hh:mm:ss or MM/dd/YYYY or hh:mm.

| Param Name | Input Type |
| :--------: | :--------: |
|    from    |    text    |

### MakeInstantFromMillis

<div block-type = "component_method" component-selector = "Clock" method-selector = "MakeInstantFromMillis" id = "clock-makeinstantfrommillis"></div>

Return Type : InstantInTime

Returns an instant in time specified by the milliseconds since 1970 in UTC.

| Param Name | Input Type |
| :--------: | :--------: |
|   millis   |   number   |

### MakeInstantFromParts

<div block-type = "component_method" component-selector = "Clock" method-selector = "MakeInstantFromParts" id = "clock-makeinstantfromparts"></div>

Return Type : InstantInTime

Returns an instant in time specified by year, month, date, hour, minute, second in UTC.

| Param Name | Input Type |
| :--------: | :--------: |
|    year    |   number   |
|    month   |   number   |
|     day    |   number   |
|    hour    |   number   |
|   minute   |   number   |
|   second   |   number   |

### MakeTime

<div block-type = "component_method" component-selector = "Clock" method-selector = "MakeTime" id = "clock-maketime"></div>

Return Type : InstantInTime

Returns an instant in time specified by hour, minute, second in UTC.

| Param Name | Input Type |
| :--------: | :--------: |
|    hour    |   number   |
|   minute   |   number   |
|   second   |   number   |

### Minute

<div block-type = "component_method" component-selector = "Clock" method-selector = "Minute" id = "clock-minute"></div>

Return Type : number

Returns the minute of the hour (0-59) from the instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### Month

<div block-type = "component_method" component-selector = "Clock" method-selector = "Month" id = "clock-month"></div>

Return Type : number

Returns the month of the year represented as a number from 1 to 12).

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### MonthName

<div block-type = "component_method" component-selector = "Clock" method-selector = "MonthName" id = "clock-monthname"></div>

Return Type : text

Returns the name of the month from the instant, e.g., January, February, March...

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### Now

<div block-type = "component_method" component-selector = "Clock" method-selector = "Now" id = "clock-now"></div>

Return Type : InstantInTime

Returns the current instant in time read from phone's clock.

### Second

<div block-type = "component_method" component-selector = "Clock" method-selector = "Second" id = "clock-second"></div>

Return Type : number

Returns the second of the minute (0-59) from the instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### SystemTime

<div block-type = "component_method" component-selector = "Clock" method-selector = "SystemTime" id = "clock-systemtime"></div>

Return Type : number

Returns the phone's internal time.

### Weekday

<div block-type = "component_method" component-selector = "Clock" method-selector = "Weekday" id = "clock-weekday"></div>

Return Type : number

Returns the day of the week represented as a number from 1 (Sunday) to 7 (Saturday).

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### WeekdayName

<div block-type = "component_method" component-selector = "Clock" method-selector = "WeekdayName" id = "clock-weekdayname"></div>

Return Type : text

Returns the name of the day of the week from the instant.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

### Year

<div block-type = "component_method" component-selector = "Clock" method-selector = "Year" id = "clock-year"></div>

Return Type : number

The year

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

## Block Properties

---

### TimerAlwaysFires

<div block-type = "component_set_get" component-selector = "Clock" property-selector = "TimerAlwaysFires" property-type = "get" id = "get-clock-timeralwaysfires"></div>

<div block-type = "component_set_get" component-selector = "Clock" property-selector = "TimerAlwaysFires" property-type = "set" id = "set-clock-timeralwaysfires"></div>

Will fire even when application is not showing on the screen if true

|    Param Name    | IO Type |
| :--------------: | :-----: |
| TimerAlwaysFires | boolean |

### TimerEnabled

<div block-type = "component_set_get" component-selector = "Clock" property-selector = "TimerEnabled" property-type = "get" id = "get-clock-timerenabled"></div>

<div block-type = "component_set_get" component-selector = "Clock" property-selector = "TimerEnabled" property-type = "set" id = "set-clock-timerenabled"></div>

Fires timer if true

|  Param Name  | IO Type |
| :----------: | :-----: |
| TimerEnabled | boolean |

### TimerInterval

<div block-type = "component_set_get" component-selector = "Clock" property-selector = "TimerInterval" property-type = "get" id = "get-clock-timerinterval"></div>

<div block-type = "component_set_get" component-selector = "Clock" property-selector = "TimerInterval" property-type = "set" id = "set-clock-timerinterval"></div>

Interval between timer events in ms

|   Param Name  | IO Type |
| :-----------: | :-----: |
| TimerInterval |  number |

## Component

---

### Clock

<div block-type = "component_component_block" component-selector = "Clock" id = "component-clock"></div>

Return Type : component

Component Clock

