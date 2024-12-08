# Importing necessary libraries
import pandas as pd
from lightgbm import LGBMClassifier
from sklearn.ensemble import AdaBoostClassifier, GradientBoostingClassifier, RandomForestClassifier
from sklearn.metrics import confusion_matrix, roc_auc_score
from sklearn.model_selection import train_test_split, cross_val_predict
from sklearn.neighbors import KNeighborsClassifier
from sklearn.tree import DecisionTreeClassifier
import sys
import os

def run_model(csv_file):
    # Reading data
    df = pd.read_csv(csv_file, index_col='id')

    # Checking for missing values
    print(df.isna().sum())

    # Data summary
    summary = df.describe()
    print(summary)

    # Feature reduction for modeling
    reduced_features = ['loc', 'v(g)', 'ev(g)', 'iv(g)', 'n', 'l', 'd', 'i', 'e', 't',
                        'lOComment', 'lOBlank', 'locCodeAndComment', 'uniq_Op', 'uniq_Opnd']

    X = df[reduced_features]
    y = df['defects']

    # Data split
    X_train, X_test, y_train, y_test = train_test_split(X, y, train_size=0.6, random_state=42)
    print(X_train.shape, X_test.shape, y_train.shape, y_test.shape)

    # Model evaluation function
    def eval(clfname, y_test, y_pred):
        con_matrx = confusion_matrix(y_test, y_pred)
        tn, fp, fn, tp = con_matrx.ravel()
        acc = (tp + tn) / (tp + tn + fp + fn) * 100
        rcall = tp / (tp + fn)
        precs = tp / (tp + fp)
        roc = roc_auc_score(y_test, y_pred)

        print(f"Confusion matrix for {clfname}:\n{con_matrx}")
        print(f"Accuracy: {acc:.2f}%")
        print(f"Recall: {rcall:.2f}")
        print(f"Precision: {precs:.2f}")
        print(f"ROC AUC: {roc:.2f}\n")

    # KNN Classifier
    knn_c = KNeighborsClassifier(n_neighbors=5)
    y_pred = cross_val_predict(knn_c, X_test, y_test, cv=5)
    eval("KNeighborsClassifier", y_test, y_pred)

    # Decision Tree Classifier
    tree_c = DecisionTreeClassifier(random_state=0)
    y_pred = cross_val_predict(tree_c, X_test, y_test, cv=5)
    eval("DecisionTreeClassifier", y_test, y_pred)

    # AdaBoost Classifier
    adaB_c = AdaBoostClassifier(n_estimators=100, random_state=0)
    y_pred = cross_val_predict(adaB_c, X_test, y_test, cv=5)
    eval("AdaBoostClassifier", y_test, y_pred)

    # Gradient Boosting Classifier
    GB_c = GradientBoostingClassifier(n_estimators=100, learning_rate=1.0, max_depth=1, random_state=0)
    y_pred = cross_val_predict(GB_c, X_test, y_test, cv=5)
    eval("GradientBoostingClassifier", y_test, y_pred)

    # RandomForest Classifier
    RF_c = RandomForestClassifier(max_depth=2, random_state=0)
    y_pred = cross_val_predict(RF_c, X_test, y_test, cv=5)
    eval("RandomForestClassifier", y_test, y_pred)

    # Scoring for each classifier
    # KNN scoring
    knn_c.fit(X_train, y_train)
    y_knn_pred = knn_c.predict_proba(X_test)[:, 1]
    roc_knn = roc_auc_score(y_test, y_knn_pred)
    print(f"KNeighborsClassifier ROC AUC Score: {roc_knn:.2f}")

    # Decision Tree scoring
    tree_c.fit(X_train, y_train)
    y_tree_pred = tree_c.predict_proba(X_test)[:, 1]
    roc_tree = roc_auc_score(y_test, y_tree_pred)
    print(f"DecisionTreeClassifier ROC AUC Score: {roc_tree:.2f}")

    # AdaBoost scoring
    adaB_c.fit(X_train, y_train)
    y_ada_pred = adaB_c.predict_proba(X_test)[:, 1]
    roc_ada = roc_auc_score(y_test, y_ada_pred)
    print(f"AdaBoostClassifier ROC AUC Score: {roc_ada:.2f}")

    # Gradient Boosting scoring
    GB_c.fit(X_train, y_train)
    y_gb_pred = GB_c.predict_proba(X_test)[:, 1]
    roc_gb = roc_auc_score(y_test, y_gb_pred)
    print(f"GradientBoostingClassifier ROC AUC Score: {roc_gb:.2f}")

    # Random Forest scoring
    RF_c.fit(X_train, y_train)
    y_rf_pred = RF_c.predict_proba(X_test)[:, 1]
    roc_rf = roc_auc_score(y_test, y_rf_pred)
    print(f"RandomForestClassifier ROC AUC Score: {roc_rf:.2f}")

if __name__ == "__main__":
    # Check if a CSV file is provided as an argument
    if len(sys.argv) < 2:
        print("Please provide the path to the CSV file.")
        sys.exit(1)

    # Run the model
    run_model(sys.argv[1])
