public class MedicineRepository {
    private MedicineDao medicineDao;
    private LiveData<List<Medicine>> todayMedicine;
    private LiveData<List<Medicine>> patientMedicine;
    private LiveData<List<Medicine>> todayLiveMedicine;
    private List<Medicine> userMedicine = new ArrayList<>();
    private LiveData<List<MedicineAlarm>> userTodayMedAlarm;
    private ExecutorService exe;
    private int patientId;

    public MedicineRepository(Application application, String today){
        AppDatabase db = AppDatabase.getInstance(application.getApplicationContext());
        this.medicineDao = db.medicineDao();
        this.todayMedicine = medicineDao.getAllLiveMedicine();
    }

    public MedicineRepository(Context context, String today, boolean timeOfTheDay){
        medicineDao = AppDatabase.getInstance(context).medicineDao();
        this.todayLiveMedicine = medicineDao.liveDataTodayMed(today);
    }

    public MedicineRepository(Context context){
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        this.medicineDao = db.medicineDao();
        this.exe = Executors.newSingleThreadExecutor();
    }

    // Constructor for the Time of the Day medicines
    public MedicineRepository(Context context, int patientId, int startHour, int startMinute, int endHour, int endMinute, String today){
        medicineDao = AppDatabase.getInstance(context).medicineDao();
        this.userTodayMedAlarm = medicineDao.getUserMedAlarm(patientId, startHour, startMinute, endHour, endMinute, today);
    }

    // Constructor for getting the PatientMeds for the Doctor View
    public MedicineRepository(Context context, int patientId){
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        this.medicineDao = db.medicineDao();
        this.exe = Executors.newSingleThreadExecutor();
        this.patientId = patientId;
        this.patientMedicine = medicineDao.getAllPatientMedicineLive(patientId);
    }

    public LiveData<List<MedicineAlarm>> getUserTodayMedAlarm() {
        return userTodayMedAlarm;
    }

    public LiveData<List<MedicineAlarm>> getMedAlarm(int patientId, int startHour, int startMinute, int endHour, int endMinute, String today) throws ExecutionException, InterruptedException {
        Future<LiveData<List<MedicineAlarm>>> future = exe.submit(() -> medicineDao.getUserMedAlarm(patientId, startHour, startMinute, endHour, endMinute, today));
        return future.get();
    }

    public void deleteMedicine(int medId){
        exe.submit(() -> medicineDao.delete(medId));
    }

    public LiveData<List<Medicine>> getTodayLiveMedicine() {
        return todayLiveMedicine;
    }

    // Insert other database operations here
    public void insertMedicine(Medicine medicine){
        exe.submit(() -> {
            if(medicineDao.doesMedExists(medicine.getMedId())){
                medicineDao.updateMed(medicine);
            } else {
                medicineDao.insert(medicine);
            }
        });
    }

    public LiveData<List<Medicine>> getPatientMedicine() {
        return patientMedicine;
    }

    public LiveData<List<Medicine>> getTodayMedicine(){
        return todayMedicine;
    }

    public List<Medicine> getTodayMedicines(String today) throws ExecutionException, InterruptedException {
        Future<List<Medicine>> future = exe.submit(() -> medicineDao.todayMed(today));
        return future.get();
    }

}
