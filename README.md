# FavThanker

This is an application used to automate thanking people for favorites on Furaffinity. Built using Java, JavaFX, and HtmlUnit.

## How it Works

First users must create a JSON file which contains the user's username, password, and an array of pre-made thank you messages he or she wishes to use as shouts to other users. When starting the program for the first time, the user then must select their JSON file. A captcha prompt is then displayed so the user can authenticate with FA. After this the user can start thanking people for favorites by pressing the 'Start' button. The application will check the user's notifications and leave shouts at the corresponding users who left favorites. The shout text content will be randomly chosen from the messages array in the JSON. The application will only send a shout if the user has not recently left a shout there (not appearing on the other user's page). In addition, it will check to make sure the user to be shouted at does not want shouts and accordingly avoids shouting at them. Once finished, the application will clear the user's favorite notifications from FA. The next time the application is run, the last user will automatically be logged in.

## System Requirements

- Windows (other OSs supported later)
- Latest Java installed
- Must use the FA beta theme

## Instructions

1. Download [FavThanker.zip](https://github.com/Seledrex/FavThanker/raw/master/FavThanker.zip)
2. Extract and place the folder in desired location
3. Edit the JSON file with a text editor
    - Fill in your FA username and password
    - Can add as many pre-made messages to the message array
4. Rename the JSON file to username.json with username being your FA username
5. Start the program by running start.bat
6. Select your JSON file with the application
7. Press start to begin thanking

## Todo
- Command line version
    
   

