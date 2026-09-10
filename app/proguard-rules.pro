# Glance 1.2.0 brings WorkManager 2.7.1, whose InputMerger consumer rule
# retains the class but loses its reflective constructor under R8 full mode.
# Keep only the merger used by Glance one-time render requests.
# Reproduced in release: InstantiationException / has no zero argument constructor.
-keep class androidx.work.OverwritingInputMerger {
    public <init>();
}
