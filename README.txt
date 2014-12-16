  _______   _                                            
 |__   __| (_)                    /\                 _   
    | |_ __ _  __ _  __ _  ___   /  \   _ __  _ __ _| |_ 
    | | '__| |/ _` |/ _` |/ _ \ / /\ \ | '_ \| '_ \_   _|
    | | |  | | (_| | (_| |  __// ____ \| |_) | |_) ||_|  
    |_|_|  |_|\__,_|\__, |\___/_/    \_\ .__/| .__/      
                     __/ |             | |   | |         
                    |___/              |_|   |_|         
----------------------------------------------------------------------------------------------------------------
About
----------------------------------------------------------------------------------------------------------------
Enhancements:
Implemented a relational database for saving data in the application in real time.
Implemented rich user interface using fragments (Fragment, ListFragment, DialogFragment, ViewPager), ActionBar navigation with custom UI elements, advanced user input validation with pop-up error messages, and added help resource on the login page.

----------------------------------------------------------------------------------------------------------------
1.0 Login Screen
----------------------------------------------------------------------------------------------------------------
Upon launch, the app takes the user to the login screen where they must enter a username and password. For Nurses, the user is redirected to the nurse screen (1.1). For Physicians, the user is redirected is to the Doctor screen (1.2). Pressing the top right information icon will display the help file.

----------------------------------------------------------------------------------------------------------------
1.1 Nurse Screen
----------------------------------------------------------------------------------------------------------------
The Nurse screen has 2 tabs. The left tab titled "Urgency", displays a list of patients sorted by urgency in descending order. The right tab titled "Sent to Doctor", displays a list of patients that have been sent to the doctor. Clicking on a patient in the list will open the Patient Screen (1.3). Use the plus icon to add and sign in a new Patient with a name, date of birth, and unique health card number. The magnifying glass icon allows the user to look up existing patients based on their health card number, and redirects to the Patient screen (1.3). The folder icon allows the user to look up a patient's medical record by their health card number and redirects to the Patient screen (1.3).

----------------------------------------------------------------------------------------------------------------
1.2 Doctor Screen 
----------------------------------------------------------------------------------------------------------------
The Doctor screen displays a list of patients who have been sent to the doctor by a Nurse. The magnifying glass icon allows the user to look up existing patients based on their health card number. Clicking on a patient in the list will open the Patient Screen (1.3).

----------------------------------------------------------------------------------------------------------------
1.3 Patient Screen
----------------------------------------------------------------------------------------------------------------
The Patient Screen displays information about the patient. For a Patient who is not signed into the ER, it displays the patient's personal information (name, date of birth, health card number). For a Patient signed into the ER, it will include information about arrival time, time seen by the doctor (if Patient was sent to the doctor) and urgency level.
The folder icon redirects to the "Medical Record" screen which displays previous visit records of that patient.
For a Patient signed into the ER, the screen will have an additonal tab: "Vitals" which displays a list of vitals sorted by time taken in descending order. The "person with a minus sign" icon signs the patient out of the ER.

Use the back button at any time to leave the Patient screen and return to the Main screen.

--------------------------------------------------------------
1.3.1 Features Unique to Nurse
--------------------------------------------------------------
For a patient not signed into the ER: 
- The "person with a plus sign" icon signs the patient in the ER.
For a patient signed in the ER: 
- The doctor icon sends the patient to the doctor.
  - The thermometer icon redirects to the "Vital Signs" screen where the user can add patient vitals.

--------------------------------------------------------------
1.3.2 Features Unique to Doctor
--------------------------------------------------------------
- Additional "Prescriptions" tab displays a list of the patient's Prescriptions.
- The medicine bottle icon redirects the user to the "Prescriptions" screen where the user can add a prescription.
