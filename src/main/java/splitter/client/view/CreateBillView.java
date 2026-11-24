package splitter.client.view;

import splitter.client.controller.BillActionController;
import splitter.client.model.BillFormModel;
import splitter.shared.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * View showing the Create Bill form and handling user inputs for bill creation.
 */
public class CreateBillView {
    private final BillActionController controller;
    private final BillFormModel form;

    public static class WeightField {
        public final String memberName;
        public final String value;
        public final boolean hasError;

        public WeightField(String memberName, String value, boolean hasError) {
            this.memberName = memberName;
            this.value = value;
            this.hasError = hasError;
        }
    }

    private String description = "";
    private String amountText = "";
    private String dateText = "";
    private String weightSumFormatted = "0.00";
    private boolean weightSumError = false;
    private boolean createEnabled = false;
    private final List<WeightField> weightFields = new ArrayList<>();

    public CreateBillView(BillActionController controller, BillFormModel form) {
        this.controller = Objects.requireNonNull(controller, "BillActionController cannot be null");
        this.form = Objects.requireNonNull(form, "BillFormModel cannot be null");
        this.form.addListener(this::refresh);
        refresh();
    }

    public void refresh() {
        this.description = form.getDescription();
        this.amountText = form.getAmountText();
        this.dateText = form.getDateText();

        weightFields.clear();
        for (String name : form.getWeightRowNames()) {
            String val = form.getWeightText(name);
            boolean err = form.hasWeightError(name);
            weightFields.add(new WeightField(name, val, err));
        }

        int sumHundredths = form.getWeightSumHundredths();
        if (sumHundredths >= 0) {
            this.weightSumFormatted = Formatting.formatWeight(sumHundredths);
            this.weightSumError = sumHundredths != 100;
        } else {
            this.weightSumFormatted = "Error";
            this.weightSumError = true;
        }

        this.createEnabled = form.isSubmitEnabled();
    }

    public void onFieldChanged(String fieldName, String memberNameOrNull, String text) {
        if ("description".equalsIgnoreCase(fieldName)) {
            controller.descriptionChanged(text);
        } else if ("amount".equalsIgnoreCase(fieldName)) {
            controller.amountChanged(text);
        } else if ("date".equalsIgnoreCase(fieldName)) {
            controller.dateChanged(text);
        } else if ("weight".equalsIgnoreCase(fieldName) && memberNameOrNull != null) {
            controller.weightChanged(memberNameOrNull, text);
        }
    }

    public void onSplitEquallyClicked() {
        controller.splitEquallyRequested();
    }

    public void onCreateClicked() {
        if (isCreateEnabled()) {
            controller.createBillRequested();
        }
    }

    public String getDescription() {
        return description;
    }

    public String getAmountText() {
        return amountText;
    }

    public String getDateText() {
        return dateText;
    }

    public String getWeightSumFormatted() {
        return weightSumFormatted;
    }

    public boolean isWeightSumError() {
        return weightSumError;
    }

    public boolean isCreateEnabled() {
        return createEnabled;
    }

    public List<WeightField> getWeightFields() {
        return weightFields;
    }
}
