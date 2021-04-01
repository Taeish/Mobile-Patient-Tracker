package com.acpc.patienttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SnapshotMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String TAG = "AC/PC";

        // All code below will run when the app is launched.
        // For all Cloud Firestore related stuff, we start of by getting our database (it works off our package name, so no login or authentication is required for now)

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // In Cloud Firestore, data is stored in collections. A collection is like a table. 'Collections' are really just folders on the firestore server.
        // Inside a collection there will be multiple documents.
        // Documents are just .json text files that act as entries.
        // In our case, every patient will have their own document which contains their ID, name, medical info etc
        // Also, firebase has a nice option to automatically name documents with a unique ID, so that should probably be used a primary key.


        // ADDING A NEW ENTRY:

        // Firestore lets you serialize custom objects to entry documents automatically. So I made an example custom class called Patient.
        // First, create a Patient:

        ArrayList illnesses = new ArrayList<String>();
        illnesses.add("TB");
        illnesses.add("Bronchitis");

        Patient patient = new Patient(2, "Ada", illnesses);

        // Now we add it to a specified collection (table) in the database with database.collection().add()
        // This way will give the new document an auto-generated unique ID as the file name. This can be used like a primary key


        database.collection("patient-data") // specify the collection name here
                .add(patient)
                // Add a success listener so we can be notified if the operation was successfuly.
                // i think success/failure listeners are optional, but if you don't use them you won't know if entry was actually added
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // If we are here, the app successfully connected to Firestore and added a new entry
                        Log.d(TAG, "SUCCESS: Added new document with ID: " + documentReference.getId());
                    }
                })
                // Add a failure listener so we can be notified if something does wrong
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If we are here, the entry could not be added for some reason (e.g no internet connection)
                        Log.w(TAG, "ERROR: Failed to add document", e);
                    }
                });




        // UPDATING AN ENTRY

        // Updating works in the same way as adding
        // Create a DocumentReference so that we can specify which document we want to update
        DocumentReference patient_1  = database.collection("patient-data").document("patient-1"); // specify collection and document name here

        // The following code updates the ID of the patient with document name 'patient-1' to 10
        patient_1.update("ID", 10)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "SUCCESS: Updated document.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "ERROR: Could not update document: ", e);
                    }
                });



        // REMOVING AN ENTRY

        // Also works in the same way as adding and updating
        database.collection("patient-data").document("patient-2")
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "SUCCESS: Deleted document.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "ERROR: Could not delete document: ", e);
                    }
                });





        // GETTING A SINGLE ENTRY (Not sure how useful this will be but it's here anyway):

        // To get a single document from Firestore, we create a DocumentReference so that we can specify which document we want to update
        // Then we call .Get() which will retrieve the specified document with an OnComplete listener.

        DocumentReference patient_0 = database.collection("patient-data").document("patient-0");
        patient_0.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    // If we are here, the app successfully connected to Firestore.
                    DocumentSnapshot document = task.getResult();

                    // Now we will check if the document exists.
                    if (document.exists()) {

                        // We now have our document. We can convert it directly into our custom Patient object:
                        Patient patient = document.toObject(Patient.class);

                        // And now we can handle the data of this patient - e.g log their data:
                        // Click '4:Run' at the bottom left of your IDE and press Ctrl + F to search for the printed patient info. You can search for the word "AC/PC"
                        Log.d(TAG, "Patient ID: " + patient.ID);
                        Log.d(TAG, "Patient name: " + patient.name);
                        Log.d(TAG, "Patient illness: " + patient.illnesses);

                    } else {
                        Log.d(TAG, "ERROR: Document not found");
                    }

                } else {

                    // If the connection task failed for some reason (e.g no internet connection), check for errors in '4:Run' tab.
                    Log.d(TAG, "ERROR: Could not connect to Firestore. Here is what went wrong: ", task.getException());
                }
            }
        });





        // GETTING MULTIPLE ENTRIES FROM COLLECTION

        database.collection("patient-data")
                .whereEqualTo("ID", "1")  // You can specify an identifier here, or remove this line to get all patients in the collection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            // If query task was successful, we can get a list of patients from it
                            ArrayList<Patient> patients = Patient.GetPatientsFromQuery(task);

                            // handle the list of patients in some way
                            for (Patient patient : patients)
                            {
                                Log.d(TAG, "Patient ID: " + patient.ID);
                                Log.d(TAG, "Patient name: " + patient.name);
                                Log.d(TAG, "Patient illness: " + patient.illnesses);
                            }

                        } else {
                            Log.d(TAG, "ERROR: Could not get documents from query", task.getException());
                        }
                    }
                });






    }
}

class Patient
{
    public int ID;
    public String name;
    public ArrayList<String> illnesses;

    public Patient(int ID, String name, ArrayList<String> illnesses)
    {
        this.ID = ID;
        this.name = name;
        this.illnesses = illnesses;
    }

    public static ArrayList<Patient> GetPatientsFromQuery(Task<QuerySnapshot> task)
    {
        ArrayList patients = new ArrayList<Patient>();

        for (QueryDocumentSnapshot document : task.getResult())
        {
            patients.add(document.toObject(Patient.class));
        }

        return patients;
    }
}
