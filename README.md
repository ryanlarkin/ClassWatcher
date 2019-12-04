# ClassWatcher
Uses University of Waterloo Open Data API to track class openings

## Set up
1. Ensure Android SDK/Java/Gradle is set up
1. Clone [eandr127/UWaterloo-API](https://github.com/eandr127/UWaterloo-API), and publish it locally
1. Get API key from [here](https://uwaterloo.ca/api/register)
1. Modify `app/src/main/res/raw/app_config.xml`. Substitute the API key in, and modify the classes to watch
1. Run `gradlew install` to put on phone