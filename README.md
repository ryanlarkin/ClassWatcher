# ClassWatcher
Uses University of Waterloo Open Data API to track class openings

## Set up
1. Ensure Android SDK/Java/Gradle is set up
1. Get API key from [here](https://uwaterloo.atlassian.net/wiki/spaces/UWAPI/pages/34025641600/Getting+Started+-+OpenAPI)
1. Modify `app/src/main/res/raw/app_config.xml`. Substitute the API key in, and modify the classes to watch
1. Run `gradlew install` to put on phone
