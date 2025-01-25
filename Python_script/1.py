import pandas as pd
from lightgbm import LGBMClassifier
from sklearn.ensemble import AdaBoostClassifier, GradientBoostingClassifier, RandomForestClassifier
from sklearn.metrics import confusion_matrix, roc_auc_score, roc_curve, accuracy_score, recall_score, precision_score
from sklearn.model_selection import train_test_split, cross_val_predict
from sklearn.neighbors import KNeighborsClassifier
from sklearn.tree import DecisionTreeClassifier
import matplotlib.pyplot as plt
import seaborn as sns
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
    def eval(clfname, y_test, y_pred, model=None, X_test=None):
        con_matrx = confusion_matrix(y_test, y_pred)
        tn, fp, fn, tp = con_matrx.ravel()
        acc = (tp + tn) / (tp + tn + fp + fn) * 100
        rcall = tp / (tp + fn)
        precs = tp / (tp + fp)

        # Use probabilities for ROC AUC if available
        if model is not None and X_test is not None and hasattr(model, "predict_proba"):
            probas = model.predict_proba(X_test)[:, 1]
            roc = roc_auc_score(y_test, probas)
        else:
            roc = roc_auc_score(y_test, y_pred)

        # Plot confusion matrix
        plt.figure(figsize=(5, 5))
        sns.heatmap(con_matrx, annot=True, fmt='d', cmap='Blues', cbar=False,
                    xticklabels=['No Defect', 'Defect'], yticklabels=['No Defect', 'Defect'])
        plt.title(f"Confusion Matrix - {clfname}")
        plt.xlabel('Predicted')
        plt.ylabel('Actual')
        # Save confusion matrix image
        plt.savefig(f"static/{clfname}_confusion_matrix.png")
        plt.close()

        # Plot ROC curve
        if model is not None and X_test is not None and hasattr(model, "predict_proba"):
            fpr, tpr, _ = roc_curve(y_test, probas)
        else:
            fpr, tpr, _ = roc_curve(y_test, y_pred)

        plt.figure(figsize=(5, 5))
        plt.plot(fpr, tpr, color='blue', label=f'{clfname} (ROC AUC = {roc:.2f})')
        plt.plot([0, 1], [0, 1], color='grey', linestyle='--')
        plt.title(f"ROC Curve - {clfname}")
        plt.xlabel('False Positive Rate')
        plt.ylabel('True Positive Rate')
        plt.legend(loc='lower right')
        # Save ROC curve image
        plt.savefig(f"static/{clfname}_roc_curve.png")
        plt.close()

        print(f"Confusion matrix for {clfname}:\n{con_matrx}")
        print(f"Accuracy: {acc:.2f}%")
        print(f"Recall: {rcall:.2f}")
        print(f"Precision: {precs:.2f}")
        print(f"ROC AUC: {roc:.2f}\n")

        return roc, acc, rcall, precs

    # Initialize classifiers
    classifiers = [
        ("KNeighborsClassifier", KNeighborsClassifier(n_neighbors=5)),
        ("DecisionTreeClassifier", DecisionTreeClassifier(random_state=0)),
        ("AdaBoostClassifier", AdaBoostClassifier(n_estimators=100, random_state=0)),
        ("GradientBoostingClassifier", GradientBoostingClassifier(n_estimators=100, learning_rate=1.0, max_depth=1, random_state=0)),
        ("RandomForestClassifier", RandomForestClassifier(max_depth=2, random_state=0))
    ]

    # Dictionary to store evaluation metrics for each classifier
    metrics = {}

    # Train and evaluate each classifier
    for clf_name, clf in classifiers:
        clf.fit(X_train, y_train)
        y_pred = cross_val_predict(clf, X_test, y_test, cv=5)

        # Get the evaluation metrics
        roc, acc, rcall, precs = eval(clf_name, y_test, y_pred, clf, X_test)

        # Store the metrics in a dictionary
        metrics[clf_name] = {
            "ROC AUC": roc,
            "Accuracy": acc,
            "Recall": rcall,
            "Precision": precs
        }

    # Print metrics for debugging before sorting
    print("\nMetrics for each model before sorting:")
    for clf_name, clf_metrics in metrics.items():
        print(f"{clf_name}: {clf_metrics}")

    # Ranking the models based on the priority criteria: ROC AUC > Precision > Recall > Accuracy
    ranked_models = sorted(metrics.items(), key=lambda item: (
        -item[1]["ROC AUC"],         # Highest ROC AUC
        -item[1]["Precision"],       # If ROC AUC is the same, highest Precision
        -item[1]["Recall"],          # If both ROC AUC and Precision are the same, highest Recall
        -item[1]["Accuracy"]         # If all the above are the same, highest Accuracy
    ))

    # Print the sorted models
    print("\nModel Ranking based on Priority (ROC AUC > Precision > Recall > Accuracy):\n")
    for i, (model_name, metrics) in enumerate(ranked_models, 1):
        print(f"{i}. {model_name}: ROC AUC = {metrics['ROC AUC']:.2f}, Accuracy = {metrics['Accuracy']:.2f}%, "
              f"Recall = {metrics['Recall']:.2f}, Precision = {metrics['Precision']:.2f}")

    # Optimized algorithm (first in the sorted list)
    optimized_algorithm = ranked_models[0][0]
    print(f"\nThe optimized algorithm based on ROC AUC, Precision, Recall, and Accuracy is: {optimized_algorithm}")

    # Plot ROC AUC comparison for all classifiers
    plt.figure(figsize=(10, 6))
    plt.bar([model[0] for model in ranked_models], [model[1]["ROC AUC"] for model in ranked_models], color='skyblue')
    plt.title("ROC AUC Comparison of Classifiers")
    plt.xlabel("Classifier")
    plt.ylabel("ROC AUC Score")
    plt.savefig("static/roc_auc_comparison.png")


if __name__ == "__main__":
    # Check if a CSV file is provided as an argument
    if len(sys.argv) < 2:
        print("Please provide the path to the CSV file.")
        sys.exit(1)

    # Run the model
    run_model(sys.argv[1])
