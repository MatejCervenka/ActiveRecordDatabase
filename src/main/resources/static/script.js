function toggleForm(formId) {
    const addForm = document.getElementById('addForm');
    const editForm = document.getElementById('editForm');

    // Hide forms if they exist
    if (addForm) addForm.style.display = 'none';
    if (editForm) editForm.style.display = 'none';

    // Show the specified form if it exists
    if (formId) {
        const formToShow = document.getElementById(formId);
        if (formToShow) {
            formToShow.style.display = 'block';
        } else {
            console.error(`Element with ID '${formId}' not found.`);
        }
    }
}
