/**
 *  Open Dimmer Lights at Set Level
 *
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
 */
definition(
    name: "Open Dimmer Lights at Set Level",
    namespace: "tigerbrain",
    author: "Bryan Go",
    description: "Always turn on lights at a preset dimming level",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@3x.png")


preferences {
    section("When a switch turns on...") {
        input "theswitch", "capability.switch", required: true
    }
    section("Dimmer brightness will be set...") {
        input "thedimmer", "capability.switchLevel", required: true
    }
    section("Brightness set at...") {
        input "thelevel", "number", required: true, title: "What Percentage (0-100)?"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(theswitch, "switch.on", switchOnHandler)
}

def switchOnHandler(evt) {
    log.debug "switchOnHandler called: $evt"
    thedimmer.setLevel(thelevel)
}