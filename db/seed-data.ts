// Seed data ported from the EatFine Android app (SeedData.kt) plus starter kitchen inventory
import type { restaurants, menuItems, reviews, inventoryItems } from "./schema.js";

export const seedRestaurants: (typeof restaurants.$inferInsert)[] = [
  {
    "id": "rest_verde",
    "name": "Verde & Grain Botanic Bistro",
    "tagline": "100% Plant-Powered, Organic & Celiac Safe",
    "cuisines": "Vegan, Healthy Bowls, Gluten-Free",
    "rating": 4.9,
    "reviewCount": 524,
    "deliveryTimeMin": 22,
    "deliveryFee": 1.99,
    "minOrder": 12.0,
    "priceTier": 2,
    "distanceKm": 1.8,
    "address": "742 Evergreen Way, Green District",
    "isPureVeg": true,
    "isPromoted": true,
    "isOpen": true,
    "offerText": "20% OFF on all Plant Bowls",
    "heroCategory": "Healthy",
    "certificationNote": "Certified 100% Vegan & Dedicated Celiac Gluten-Free Prep Station.",
    "dietary": [
      "VEGETARIAN",
      "VEGAN",
      "GLUTEN_FREE",
      "ORGANIC",
      "DAIRY_FREE"
    ],
    "image": "/images/promo.jpg"
  },
  {
    "id": "rest_saffron",
    "name": "Saffron Royal Tandoori & Biryani",
    "tagline": "Authentic Heritage Dum Biryani & Kebabs",
    "cuisines": "Indian, Mughlai, Biryani, Halal",
    "rating": 4.8,
    "reviewCount": 890,
    "deliveryTimeMin": 28,
    "deliveryFee": 2.49,
    "minOrder": 15.0,
    "priceTier": 2,
    "distanceKm": 2.4,
    "address": "120 Royal Heritage Avenue",
    "isPureVeg": false,
    "isPromoted": true,
    "isOpen": true,
    "offerText": "Free Garlic Naan with Orders $25+",
    "heroCategory": "Biryani",
    "certificationNote": "100% Certified Halal Meat and separate Vegetarian preparation tandoor.",
    "dietary": [
      "HALAL",
      "VEGETARIAN",
      "NUT_FREE"
    ],
    "image": "/images/hero.jpg"
  },
  {
    "id": "rest_shanti",
    "name": "Shanti Satvik Bhojanalaya",
    "tagline": "Pure Jain & Ayurvedic Wholesome Dining",
    "cuisines": "Pure Veg, Jain, North Indian, Thali",
    "rating": 4.9,
    "reviewCount": 380,
    "deliveryTimeMin": 20,
    "deliveryFee": 0.0,
    "minOrder": 10.0,
    "priceTier": 1,
    "distanceKm": 1.2,
    "address": "45 Temple Crescent, Heritage Nagar",
    "isPureVeg": true,
    "isPromoted": false,
    "isOpen": true,
    "offerText": "Free Delivery on Satvik Thali",
    "heroCategory": "Thali",
    "certificationNote": "100% Jain Pure: Zero onion, zero garlic, zero root vegetables.",
    "dietary": [
      "VEGETARIAN",
      "JAIN",
      "ORGANIC",
      "NUT_FREE"
    ],
    "image": "/images/promo.jpg"
  },
  {
    "id": "rest_crust",
    "name": "Crust & Copper Artisan Pizzeria",
    "tagline": "Stone-Baked Sourdough & Celiac Crusts",
    "cuisines": "Italian, Pizza, Pasta, Gluten-Free",
    "rating": 4.7,
    "reviewCount": 640,
    "deliveryTimeMin": 30,
    "deliveryFee": 2.99,
    "minOrder": 14.0,
    "priceTier": 2,
    "distanceKm": 3.1,
    "address": "88 Little Italy Boulevard",
    "isPureVeg": false,
    "isPromoted": false,
    "isOpen": true,
    "offerText": "Buy 1 Artisan Pizza, Get 2nd 50% Off",
    "heroCategory": "Pizza",
    "certificationNote": "Separate dedicated oven for Gluten-Free cauliflower and almond crusts.",
    "dietary": [
      "GLUTEN_FREE",
      "VEGETARIAN",
      "DAIRY_FREE"
    ],
    "image": "/images/hero.jpg"
  },
  {
    "id": "rest_zenith",
    "name": "Zenith Clean Protein & Keto Lab",
    "tagline": "Macros-Tracked, Zero Sugar & Low-Carb",
    "cuisines": "Keto, Healthy, Salad, Bowls",
    "rating": 4.8,
    "reviewCount": 412,
    "deliveryTimeMin": 18,
    "deliveryFee": 1.49,
    "minOrder": 12.0,
    "priceTier": 3,
    "distanceKm": 1.5,
    "address": "500 Innovation Park, Tech Hub",
    "isPureVeg": false,
    "isPromoted": true,
    "isOpen": true,
    "offerText": "15% OFF for EatFine Plus Members",
    "heroCategory": "Healthy",
    "certificationNote": "Under 8g net carbs per entree, keto-certified cooking oils.",
    "dietary": [
      "KETO",
      "GLUTEN_FREE",
      "DAIRY_FREE",
      "HALAL"
    ],
    "image": "/images/promo.jpg"
  },
  {
    "id": "rest_aura",
    "name": "Aura Mediterranean Mezze & Grill",
    "tagline": "Sun-Kissed Olives, Kebabs & Fresh Dips",
    "cuisines": "Mediterranean, Greek, Halal, Vegan",
    "rating": 4.7,
    "reviewCount": 530,
    "deliveryTimeMin": 25,
    "deliveryFee": 1.99,
    "minOrder": 15.0,
    "priceTier": 2,
    "distanceKm": 2.9,
    "address": "21 Harbor Promenade",
    "isPureVeg": false,
    "isPromoted": false,
    "isOpen": true,
    "offerText": "Free Hummus & Pita with orders over $30",
    "heroCategory": "Mediterranean",
    "certificationNote": "Halal Certified meats & wide array of Mediterranean vegan mezze.",
    "dietary": [
      "HALAL",
      "VEGAN",
      "VEGETARIAN",
      "GLUTEN_FREE",
      "ORGANIC"
    ],
    "image": "/images/hero.jpg"
  },
  {
    "id": "rest_wholesome",
    "name": "Wholesome Hearth Gluten-Free Bakery",
    "tagline": "100% Celiac Safe Artisan Bakes & Treats",
    "cuisines": "Bakery, Desserts, Coffee, Gluten-Free",
    "rating": 4.9,
    "reviewCount": 310,
    "deliveryTimeMin": 15,
    "deliveryFee": 1.99,
    "minOrder": 8.0,
    "priceTier": 2,
    "distanceKm": 1.1,
    "address": "14 Baker’s Mews, Old Town",
    "isPureVeg": true,
    "isPromoted": false,
    "isOpen": true,
    "offerText": "Free Matcha Cookie with Coffee",
    "heroCategory": "Bakery",
    "certificationNote": "Zero gluten facility. Dedicated nut-free clean room.",
    "dietary": [
      "GLUTEN_FREE",
      "VEGETARIAN",
      "DAIRY_FREE",
      "NUT_FREE"
    ],
    "image": "/images/promo.jpg"
  }
];

export const seedMenuItems: (typeof menuItems.$inferInsert)[] = [
  {
    "id": "m_verde_1",
    "restaurantId": "rest_verde",
    "name": "Avocado Goddess Glow Bowl",
    "description": "Tricolor quinoa, Hass avocado, crisp cucumbers, pickled radishes, hemp hearts, turmeric tahini dressing.",
    "price": 13.99,
    "category": "Bowls",
    "isVeg": true,
    "calories": 420,
    "spiceLevel": 0,
    "isBestseller": true,
    "rating": 4.9,
    "dietary": [
      "VEGAN",
      "VEGETARIAN",
      "GLUTEN_FREE",
      "ORGANIC"
    ]
  },
  {
    "id": "m_verde_2",
    "restaurantId": "rest_verde",
    "name": "Wild Truffle & Mushroom Risotto",
    "description": "Arborio rice cooked in slow herb broth with chanterelles, porcini, vegan cashew cream and thyme oil.",
    "price": 16.5,
    "category": "Mains",
    "isVeg": true,
    "calories": 510,
    "spiceLevel": 0,
    "isBestseller": true,
    "rating": 4.8,
    "dietary": [
      "VEGAN",
      "VEGETARIAN",
      "GLUTEN_FREE",
      "DAIRY_FREE"
    ]
  },
  {
    "id": "m_verde_3",
    "restaurantId": "rest_verde",
    "name": "Green Vitality Cold-Pressed Elixir",
    "description": "Organic kale, green apple, cucumber, ginger, celery, mint, freshly cold-pressed.",
    "price": 6.99,
    "category": "Beverages",
    "isVeg": true,
    "calories": 110,
    "spiceLevel": 0,
    "isBestseller": false,
    "rating": 4.7,
    "dietary": [
      "VEGAN",
      "VEGETARIAN",
      "GLUTEN_FREE",
      "ORGANIC"
    ]
  },
  {
    "id": "m_saff_1",
    "restaurantId": "rest_saffron",
    "name": "Royal Dum Chicken Biryani (Halal)",
    "description": "Certified Halal tender chicken marinated in saffron, caramelized onions, layered with aged basmati rice.",
    "price": 16.99,
    "category": "Biryani",
    "isVeg": false,
    "calories": 680,
    "spiceLevel": 2,
    "isBestseller": true,
    "rating": 4.9,
    "dietary": [
      "HALAL",
      "NUT_FREE"
    ]
  },
  {
    "id": "m_saff_2",
    "restaurantId": "rest_saffron",
    "name": "Paneer Tikka Angara",
    "description": "Smoked cottage cheese cubes marinated in Kashmiri chili, hung curd, roasted gram flour and carom seeds.",
    "price": 14.5,
    "category": "Starters",
    "isVeg": true,
    "calories": 490,
    "spiceLevel": 2,
    "isBestseller": false,
    "rating": 4.8,
    "dietary": [
      "VEGETARIAN",
      "HALAL",
      "GLUTEN_FREE"
    ]
  },
  {
    "id": "m_saff_3",
    "restaurantId": "rest_saffron",
    "name": "Dal Bukhara Slow-Simmered",
    "description": "Black lentils simmered overnight over slow charcoal embers with organic butter and sun-ripened tomatoes.",
    "price": 13.0,
    "category": "Mains",
    "isVeg": true,
    "calories": 390,
    "spiceLevel": 1,
    "isBestseller": true,
    "rating": 4.9,
    "dietary": [
      "VEGETARIAN",
      "GLUTEN_FREE",
      "HALAL"
    ]
  },
  {
    "id": "m_jain_1",
    "restaurantId": "rest_shanti",
    "name": "Shanti Royal Satvik Thali (Jain)",
    "description": "Pure Jain feast: Raw banana sabzi, yellow moong dal tadka, fresh phulkas, jeera rice, buttermilk, shrikhand. No onion/garlic/root.",
    "price": 14.99,
    "category": "Thali",
    "isVeg": true,
    "calories": 590,
    "spiceLevel": 1,
    "isBestseller": true,
    "rating": 5.0,
    "dietary": [
      "VEGETARIAN",
      "JAIN",
      "ORGANIC",
      "NUT_FREE"
    ]
  },
  {
    "id": "m_jain_2",
    "restaurantId": "rest_shanti",
    "name": "Jain Paneer Butter Masala",
    "description": "Fresh cottage cheese in creamy tomato-cashew gravy made without onion or garlic, finished with kasuri methi.",
    "price": 13.5,
    "category": "Mains",
    "isVeg": true,
    "calories": 480,
    "spiceLevel": 1,
    "isBestseller": false,
    "rating": 4.8,
    "dietary": [
      "VEGETARIAN",
      "JAIN",
      "GLUTEN_FREE"
    ]
  },
  {
    "id": "m_crust_1",
    "restaurantId": "rest_crust",
    "name": "Gluten-Free Margherita Botanica",
    "description": "Crispy cauliflower-almond crust, San Marzano tomato coulis, vegan cashew mozzarella, fresh sweet basil.",
    "price": 17.5,
    "category": "Pizza",
    "isVeg": true,
    "calories": 490,
    "spiceLevel": 0,
    "isBestseller": true,
    "rating": 4.8,
    "dietary": [
      "GLUTEN_FREE",
      "VEGAN",
      "VEGETARIAN",
      "DAIRY_FREE"
    ]
  },
  {
    "id": "m_crust_2",
    "restaurantId": "rest_crust",
    "name": "Diavola Truffle Rustica",
    "description": "Spicy artisanal soppressata, smoked fior di latte, hot honey drizzle, organic oregano.",
    "price": 18.99,
    "category": "Pizza",
    "isVeg": false,
    "calories": 720,
    "spiceLevel": 2,
    "isBestseller": true,
    "rating": 4.7,
    "dietary": [
      "NUT_FREE"
    ]
  },
  {
    "id": "m_zen_1",
    "restaurantId": "rest_zenith",
    "name": "Keto Herb-Seared Salmon & Asparagus",
    "description": "Wild Atlantic salmon fillet seared in grass-fed ghee, charred lemon asparagus, garlic-dill cauliflower puree (4g net carbs).",
    "price": 19.99,
    "category": "Mains",
    "isVeg": false,
    "calories": 540,
    "spiceLevel": 0,
    "isBestseller": true,
    "rating": 4.9,
    "dietary": [
      "KETO",
      "GLUTEN_FREE",
      "DAIRY_FREE",
      "HALAL"
    ]
  },
  {
    "id": "m_zen_2",
    "restaurantId": "rest_zenith",
    "name": "Avocado Bacon Keto Crunch Salad",
    "description": "Baby spinach, crispy bacon strips, ripe avocado, soft boiled farm egg, walnut oil vinaigrette (3g net carbs).",
    "price": 14.5,
    "category": "Bowls",
    "isVeg": false,
    "calories": 460,
    "spiceLevel": 0,
    "isBestseller": false,
    "rating": 4.8,
    "dietary": [
      "KETO",
      "GLUTEN_FREE",
      "DAIRY_FREE"
    ]
  },
  {
    "id": "m_bake_1",
    "restaurantId": "rest_wholesome",
    "name": "Gluten-Free Cinnamon Swirl Brioche",
    "description": "Soft, fluffy celiac-certified cinnamon brioche roll iced with Madagascar vanilla glaze.",
    "price": 5.5,
    "category": "Desserts",
    "isVeg": true,
    "calories": 290,
    "spiceLevel": 0,
    "isBestseller": true,
    "rating": 4.9,
    "dietary": [
      "GLUTEN_FREE",
      "VEGETARIAN",
      "NUT_FREE"
    ]
  },
  {
    "id": "m_bake_2",
    "restaurantId": "rest_wholesome",
    "name": "Vegan Dark Chocolate Hazelnut Tart",
    "description": "72% dark Valrhona ganache in an almond crust, topped with sea salt flakes.",
    "price": 6.99,
    "category": "Desserts",
    "isVeg": true,
    "calories": 340,
    "spiceLevel": 0,
    "isBestseller": true,
    "rating": 5.0,
    "dietary": [
      "VEGAN",
      "VEGETARIAN",
      "GLUTEN_FREE",
      "DAIRY_FREE"
    ]
  }
];

export const seedReviews: (typeof reviews.$inferInsert)[] = [
  {
    "restaurantId": "rest_verde",
    "userName": "Samantha W.",
    "rating": 5.0,
    "comment": "As someone with severe celiac, finding delicious gluten-free vegan food without worrying about cross-contamination is incredible. The Avocado Goddess Bowl was heavenly!",
    "dietaryTags": "Gluten-Free, Vegan"
  },
  {
    "restaurantId": "rest_shanti",
    "userName": "Aarav Mehta",
    "rating": 5.0,
    "comment": "Authentic Jain food prepared with such devotion! Completely onion and garlic free, and the taste is royal. EatFine makes ordering Jain food so easy!",
    "dietaryTags": "Jain, Pure Veg"
  },
  {
    "restaurantId": "rest_saffron",
    "userName": "Tariq K.",
    "rating": 5.0,
    "comment": "Verified Halal certification and mouthwatering aromatic dum biryani! Delivery was under 25 minutes, still piping hot.",
    "dietaryTags": "Halal"
  },
  {
    "restaurantId": "rest_verde",
    "userName": "Marcus Brody",
    "rating": 4.5,
    "comment": "The Truffle Mushroom Risotto was rich and creamy without using any dairy. Incredible culinary technique!",
    "dietaryTags": "Vegan, Dairy-Free"
  },
  {
    "restaurantId": "rest_crust",
    "userName": "Elena Rostova",
    "rating": 5.0,
    "comment": "Celiac heaven! Never felt safer ordering takeout. Crispy fish tacos with zero gluten contamination.",
    "dietaryTags": "Gluten-Free, Dairy-Free"
  },
  {
    "restaurantId": "rest_zenith",
    "userName": "David Cho",
    "rating": 4.8,
    "comment": "Macros were accurate, no sneaky carbs or hidden sugar. Grass-fed smash burger was super juicy.",
    "dietaryTags": "Keto, Gluten-Free"
  },
  {
    "restaurantId": "rest_aura",
    "userName": "Hina Patel",
    "rating": 4.7,
    "comment": "Loved that they have a dedicated vegetarian ramen broth and tamari soy sauce. Delicious umami flavor!",
    "dietaryTags": "Vegetarian, Halal"
  }
];

export const seedInventory: (typeof inventoryItems.$inferInsert)[] = [
  {
    "restaurantId": "rest_verde",
    "sku": "VG-QUIN",
    "name": "Tricolor Quinoa",
    "category": "Dry Goods",
    "unit": "kg",
    "quantity": 18,
    "reorderLevel": 8,
    "unitCost": 6.4,
    "supplier": "GreenField Organics"
  },
  {
    "restaurantId": "rest_verde",
    "sku": "VG-AVO",
    "name": "Hass Avocados",
    "category": "Produce",
    "unit": "pcs",
    "quantity": 40,
    "reorderLevel": 30,
    "unitCost": 1.1,
    "supplier": "Sunrise Produce Co."
  },
  {
    "restaurantId": "rest_verde",
    "sku": "VG-TAHI",
    "name": "Turmeric Tahini",
    "category": "Sauces",
    "unit": "L",
    "quantity": 3,
    "reorderLevel": 4,
    "unitCost": 12.5,
    "supplier": "Verde Kitchen Prep"
  },
  {
    "restaurantId": "rest_verde",
    "sku": "VG-KALE",
    "name": "Organic Kale",
    "category": "Produce",
    "unit": "kg",
    "quantity": 6,
    "reorderLevel": 5,
    "unitCost": 4.2,
    "supplier": "GreenField Organics"
  },
  {
    "restaurantId": "rest_saffron",
    "sku": "SR-BASM",
    "name": "Aged Basmati Rice",
    "category": "Dry Goods",
    "unit": "kg",
    "quantity": 45,
    "reorderLevel": 20,
    "unitCost": 3.8,
    "supplier": "Royal Grains Ltd."
  },
  {
    "restaurantId": "rest_saffron",
    "sku": "SR-CHKN",
    "name": "Halal Chicken Thigh",
    "category": "Protein",
    "unit": "kg",
    "quantity": 12,
    "reorderLevel": 15,
    "unitCost": 8.9,
    "supplier": "Crescent Halal Meats"
  },
  {
    "restaurantId": "rest_saffron",
    "sku": "SR-SAFF",
    "name": "Kashmiri Saffron",
    "category": "Spices",
    "unit": "g",
    "quantity": 60,
    "reorderLevel": 25,
    "unitCost": 0.9,
    "supplier": "Spice Route Traders"
  },
  {
    "restaurantId": "rest_saffron",
    "sku": "SR-PANR",
    "name": "Fresh Paneer",
    "category": "Dairy",
    "unit": "kg",
    "quantity": 9,
    "reorderLevel": 6,
    "unitCost": 9.5,
    "supplier": "Anand Dairy"
  },
  {
    "restaurantId": "rest_shanti",
    "sku": "SS-MOON",
    "name": "Yellow Moong Dal",
    "category": "Dry Goods",
    "unit": "kg",
    "quantity": 25,
    "reorderLevel": 10,
    "unitCost": 2.9,
    "supplier": "Satvik Staples"
  },
  {
    "restaurantId": "rest_shanti",
    "sku": "SS-BANA",
    "name": "Raw Banana",
    "category": "Produce",
    "unit": "kg",
    "quantity": 4,
    "reorderLevel": 6,
    "unitCost": 2.1,
    "supplier": "Sunrise Produce Co."
  },
  {
    "restaurantId": "rest_shanti",
    "sku": "SS-ATTA",
    "name": "Whole Wheat Atta",
    "category": "Dry Goods",
    "unit": "kg",
    "quantity": 30,
    "reorderLevel": 15,
    "unitCost": 1.6,
    "supplier": "Satvik Staples"
  },
  {
    "restaurantId": "rest_crust",
    "sku": "CC-CAUL",
    "name": "Cauliflower-Almond Crust Base",
    "category": "Bakery",
    "unit": "pcs",
    "quantity": 35,
    "reorderLevel": 20,
    "unitCost": 2.4,
    "supplier": "Copper Kitchen Prep"
  },
  {
    "restaurantId": "rest_crust",
    "sku": "CC-SANM",
    "name": "San Marzano Tomatoes",
    "category": "Canned",
    "unit": "kg",
    "quantity": 22,
    "reorderLevel": 10,
    "unitCost": 4.5,
    "supplier": "Italia Imports"
  },
  {
    "restaurantId": "rest_crust",
    "sku": "CC-FIOR",
    "name": "Fior di Latte",
    "category": "Dairy",
    "unit": "kg",
    "quantity": 5,
    "reorderLevel": 8,
    "unitCost": 11.0,
    "supplier": "Latteria Bella"
  },
  {
    "restaurantId": "rest_crust",
    "sku": "CC-BASL",
    "name": "Sweet Basil",
    "category": "Produce",
    "unit": "bunch",
    "quantity": 14,
    "reorderLevel": 10,
    "unitCost": 1.5,
    "supplier": "Sunrise Produce Co."
  },
  {
    "restaurantId": "rest_zenith",
    "sku": "ZK-SALM",
    "name": "Wild Atlantic Salmon",
    "category": "Protein",
    "unit": "kg",
    "quantity": 8,
    "reorderLevel": 10,
    "unitCost": 24.0,
    "supplier": "North Sea Fisheries"
  },
  {
    "restaurantId": "rest_zenith",
    "sku": "ZK-GHEE",
    "name": "Grass-Fed Ghee",
    "category": "Dairy",
    "unit": "kg",
    "quantity": 6,
    "reorderLevel": 3,
    "unitCost": 15.5,
    "supplier": "Pure Pastures"
  },
  {
    "restaurantId": "rest_zenith",
    "sku": "ZK-ASPA",
    "name": "Asparagus",
    "category": "Produce",
    "unit": "kg",
    "quantity": 7,
    "reorderLevel": 5,
    "unitCost": 7.8,
    "supplier": "Sunrise Produce Co."
  },
  {
    "restaurantId": "rest_aura",
    "sku": "AM-CHKP",
    "name": "Chickpeas",
    "category": "Dry Goods",
    "unit": "kg",
    "quantity": 20,
    "reorderLevel": 10,
    "unitCost": 2.2,
    "supplier": "Levant Pantry"
  },
  {
    "restaurantId": "rest_aura",
    "sku": "AM-OLIV",
    "name": "Kalamata Olives",
    "category": "Canned",
    "unit": "kg",
    "quantity": 9,
    "reorderLevel": 4,
    "unitCost": 9.6,
    "supplier": "Aegean Imports"
  },
  {
    "restaurantId": "rest_aura",
    "sku": "AM-PITA",
    "name": "Fresh Pita",
    "category": "Bakery",
    "unit": "pcs",
    "quantity": 60,
    "reorderLevel": 80,
    "unitCost": 0.4,
    "supplier": "Harbor Bakehouse"
  },
  {
    "restaurantId": "rest_wholesome",
    "sku": "WH-GFFL",
    "name": "Celiac-Safe Flour Blend",
    "category": "Dry Goods",
    "unit": "kg",
    "quantity": 28,
    "reorderLevel": 15,
    "unitCost": 5.2,
    "supplier": "PureGrain GF"
  },
  {
    "restaurantId": "rest_wholesome",
    "sku": "WH-VALR",
    "name": "Valrhona 72% Chocolate",
    "category": "Baking",
    "unit": "kg",
    "quantity": 4,
    "reorderLevel": 5,
    "unitCost": 28.0,
    "supplier": "Chocolatier Supply"
  },
  {
    "restaurantId": "rest_wholesome",
    "sku": "WH-VANI",
    "name": "Madagascar Vanilla",
    "category": "Baking",
    "unit": "ml",
    "quantity": 400,
    "reorderLevel": 250,
    "unitCost": 0.12,
    "supplier": "Spice Route Traders"
  }
];
