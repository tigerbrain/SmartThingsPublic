/**
 *  Copyright 2017 Bryan Go
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Motion Light Activator
 *
 *  Author: Bryan Go
 *
 */
definition(
    name: "Motion Light Activator",
    namespace: "tigerbrain",
    author: "Bryan Go",
    description: "Turn on a light if its switch was not toggled for some time and motion is detected during a selected period of time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@3x.png"
)

preferences {
	section("Turn on this light..."){
		input "lights", "capability.switch", required: true
	}
	section("If the switch was not toggled in the past..."){
		input "minInactiveMinutes", "number", title: "Minutes?", required: true
    }
	section("And motion is detected..."){
		input "motionSensor", "capability.motionSensor", title: "Where?", required: true
	}
	section("And the light level reported..."){
		input "lightSensor", "capability.illuminanceMeasurement", title: "Where?", required: false
	}
	section ("Does not surpass brightness..."){
		input "weakLightLevel", "number", title: "Percentage?", required: false
	}
    section ("Between these specific times of the day...") {
        input "fromTime", "time", title: "From", required: false
        input "toTime", "time", title: "To", required: false
    }
    section("For these specific days of the week...") {
        input "days", "enum", title: "Which?", required: false, multiple: true, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
    }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motionSensor, "motion.active", motionHandler)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"

    def currMillisecs = now()
    def lightStateObj = lights.currentState("switch")
    def lightState = lightStateObj.value
    
    if (lightState == "on") {
        log.debug "Failed state check: Switch is already ${lightState}"
        return
    }    
    
    def lastActivity = lightStateObj.date
    def lastActivityMillisecs = lastActivity.getTime()
    def minInactiveMillisecs = timeOffset(minInactiveMinutes)
    def timeDiff = currMillisecs - lastActivityMillisecs
    if (timeDiff <= minInactiveMillisecs) {
        log.debug "Failed minimum activity check: Switch was last active on: ${lastActivity}"
        return
    }
    
    if (lightSensor) {
        def weakLight = 100
        if (weakLightLevel) {
            weakLight = weakLightLevel
        }
        def currentLight = lightSensor.currentilluminance
	    if (currentLight > weakLight) {
            log.debug "Failed light check: Brightness level ${currentLight} surpasses weak light level ${weakLight}"
            return
        }
    }
    
    if (days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        // Ensure the new date object is set to local time zone
        df.setTimeZone(location.timeZone)
        def day = df.format(new Date())
        // Does the preference input Days, i.e., days-of-week, contain today?
        def dayCheck = days.contains(day)
        if (!dayCheck) {
            log.debug "Failed day check: Current day not within the selected day of the week"
            return
        }
    }
    
    if (fromTime && toTime) {
        def withinTimeFrame = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
        if (!withinTimeFrame) {
            log.debug "Failed time check: Current time not within the selected time of the day"
            return
        }
    }
        
    lights.on()
    log.debug "Passed all checks, light activated"
	
}