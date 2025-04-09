package cit.edu.wildcanteen.repositories

import cit.edu.wildcanteen.FoodItem
import com.google.firebase.firestore.FirebaseFirestore

object FoodRepository {

    fun getPopularFoodList(): List<FoodItem> {
        return listOf(
            FoodItem("Main Meal", 1,"Inasal Chicken", 99.00, 4.8, "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", "https://ik.imagekit.io/kylezzz/drawable/chicken.png", true),
            FoodItem("Main Meal",2, "Palabok", 80.00, 4.9, "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", "https://ik.imagekit.io/kylezzz/drawable/palabok.png?updatedAt=1743247449304", true,),
            FoodItem("Main Meal", 3,"Lumpia", 50.00, 4.6, "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", "https://ik.imagekit.io/kylezzz/drawable/lumpia.png?updatedAt=1743247448996", true)
        )
    }

    fun getAllFoodList(): List<FoodItem> {
        return listOf(
            FoodItem("Main Meal", 4,"Sinigang na Baboy", 120.00, 4.9, "Sour pork soup with vegetables, perfect for rainy days!", "https://ik.imagekit.io/kylezzz/drawable/sinigang.png?updatedAt=1743247451949", false),
            FoodItem("Main Meal", 5,"Adobo (Chicken/Pork)", 110.00, 4.8, "Classic soy sauce and vinegar stew with tender meat.", "https://ik.imagekit.io/kylezzz/drawable/adobo.jpg?updatedAt=1743247443868", false),
            FoodItem("Main Meal", 6,"Bicol Express", 115.00, 4.7, "Spicy coconut-based pork dish with chili peppers.","https://ik.imagekit.io/kylezzz/drawable/bicol_express.jpg?updatedAt=1743247444186",  false),
            FoodItem("Main Meal", 7,"Pancit Canton", 20.00, 4.6, "Stir-fried noodles with vegetables and meat.", "https://ik.imagekit.io/kylezzz/drawable/pancit_canton.png?updatedAt=1743247449538",  false),
            FoodItem("Main Meal", 8,"Lechon Kawali", 130.00, 4.9, "Crispy deep-fried pork belly served with liver sauce.", "https://ik.imagekit.io/kylezzz/drawable/lechon_kawali.jpg?updatedAt=1743247448757",  false),

            FoodItem("Snack & Side", 9,"Empanada", 45.00, 4.6, "Crispy pastry filled with meat, egg, and vegetables.","https://ik.imagekit.io/kylezzz/drawable/empanada.png?updatedAt=1743247444157",  false),
            FoodItem("Snack & Side", 10,"Turon", 35.00, 4.8, "Caramelized banana wrapped in a crunchy lumpia wrapper.", "https://ik.imagekit.io/kylezzz/drawable/turon.png?updatedAt=1743247453191",  false),
            FoodItem("Snack & Side", 11,"Kwek-Kwek", 30.00, 4.7, "Quail eggs coated in orange batter and deep-fried.","https://ik.imagekit.io/kylezzz/drawable/kwek_kwek.png?updatedAt=1743247448617",  false),
            FoodItem("Snack & Side", 12,"Chicharon Bulaklak", 60.00, 4.5, "Crispy deep-fried pork intestines, perfect with vinegar dip.", "https://ik.imagekit.io/kylezzz/drawable/chicharon_bulaklak.jpg?updatedAt=1743247443961",  false),
            FoodItem("Snack & Side", 13,"Tokwa’t Baboy", 70.00, 4.6, "Fried tofu and pork with a tangy soy-vinegar sauce.", "https://ik.imagekit.io/kylezzz/drawable/tokwa_baboy.jpg?updatedAt=1743247453169",  false),

            FoodItem("Beverage", 14,"Sago’t Gulaman", 30.00, 4.8, "Sweet iced drink with tapioca pearls and jelly.","https://ik.imagekit.io/kylezzz/drawable/sagot_gulaman.jpg?updatedAt=1743247448830",  false),
            FoodItem("Beverage", 15,"Buko Juice", 35.00, 4.9, "Fresh coconut juice, naturally refreshing!", "https://ik.imagekit.io/kylezzz/drawable/buko_juice.jpg?updatedAt=1743247443957",  false),
            FoodItem("Beverage", 16,"Calamansi Juice", 30.00, 4.6, "Filipino lemon juice, perfect for boosting immunity.","https://ik.imagekit.io/kylezzz/drawable/calamansi_juice.jpg?updatedAt=1743247443990",  false),
            FoodItem("Beverage", 17,"Gulaman at Sago Shake", 50.00, 4.8, "Creamy, blended version of the classic street drink.", "https://ik.imagekit.io/kylezzz/drawable/gulaman_shake.jpg?updatedAt=1743247448555",  false),

            FoodItem("Dessert", 18,"Leche Flan", 50.00, 4.9, "Creamy caramel custard, melt-in-your-mouth goodness!","https://ik.imagekit.io/kylezzz/drawable/leche_flan.jpg?updatedAt=1743247448836",  false),
            FoodItem("Dessert", 19,"Halo-Halo", 90.00, 4.8, "Shaved ice dessert with mixed sweet ingredients and leche flan.", "https://ik.imagekit.io/kylezzz/drawable/halo_halo.jpg?updatedAt=1743247448727", false),
            FoodItem("Dessert", 20,"Ube Halaya", 60.00, 4.7, "Purple yam jam, rich and sweet!", "https://ik.imagekit.io/kylezzz/drawable/ube_halaya.png?updatedAt=1743247453260",  false),
            FoodItem("Dessert", 21,"Cassava Cake", 45.00, 4.7, "Chewy, coconut-flavored cassava dessert.", "https://ik.imagekit.io/kylezzz/drawable/cassava_cake.jpeg?updatedAt=1743247444120",  false)
        )
    }

    fun uploadAllFoodItems(foodList: List<FoodItem>, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        foodList.forEach { foodItem ->
            val docRef = db.collection("food_items").document()
            batch.set(docRef, foodItem)
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
