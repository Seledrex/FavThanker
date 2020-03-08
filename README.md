# FavThanker

This is an application used to automate thanking people for favorites on Furaffinity.

## Specifications

- Java 11
- JavaFX 11
- HtmlUnit

## System Requirements

- Windows or Linux
- Java installed
- Must use the FA beta theme

## Instructions

1. Download [FavThanker.zip](https://github.com/Seledrex/FavThanker/raw/master/FavThanker.zip)
2. Extract and place the folder in desired location
3. Edit the JSON file with a text editor
    - Fill in your FA username and password
    - Add pre-made messages to the message array
    - Add groups for sending specific messages to specific groups of users
4. Rename the JSON file to username.json with username being your FA username
5. Run the executable jar file corresponding to your operating system
5. Select your JSON file within the application
6. Press start to begin thanking

## Notes

- Favorite notifications will be automatically removed from your FA
- Favorite notifications and shouts will be saved in CSV format in the application folder
- Cookies are saved to the application folder so the user will stay logged in
- The application will not leave a shout on user pages where the user has left a shout in their own shout box
- Shouts are made every 20 seconds to avoid FA spam warnings

## Compiling

`mvn clean compile assembly:single`

## Todo
- Fix for new FA update
- Command line version
    
   

