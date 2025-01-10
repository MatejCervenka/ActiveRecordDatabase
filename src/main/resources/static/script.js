function toggleForm(formId) {
    document.getElementById('addForm').style.display = 'none';
    document.getElementById('editForm').style.display = 'none';
    if (formId) {
        document.getElementById(formId).style.display = 'block';
    }
}