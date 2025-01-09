function toggleForm(formId) {
    document.getElementById('addForm').style.display = 'none';
    document.getElementById('editForm').style.display = 'none';
    if (formId) {
        document.getElementById(formId).style.display = 'block';
    }
}

/*
function openEditForm(productId) {
    const product = /!* Find product object in products list using productId *!/;
    document.getElementById('editForm').style.display = 'block';

    document.getElementById('editName').value = product.name;
    document.getElementById('editPrice').value = product.price;
    document.getElementById('editStock').value = product.stock;
    document.getElementById('editCategory').value = product.categoryId;

    // Set the hidden product ID field
    document.querySelector('input[name="id"]').value = productId;
}*/
