## uncomment the following two lines to install the required libraries
#install.packages("pmml")
#install.packages("randomForest")

library(pmml)
library(randomForest)

## load the "baseline" reference data

dat_folder <- './data'
data <- read.table(file=paste(dat_folder, "orders.tsv", sep="/"), sep="\t", quote="", na.strings="NULL", header=TRUE, encoding="UTF8")

dim(data)
head(data)

## split data into test and train sets

set.seed(71)
split_ratio <- 2/10
split <- round(dim(data)[1] * split_ratio)

data_tests <- data[1:split,]
dim(data_tests)
print(table(data_tests[,"label"]))

data_train <- data[(split + 1):dim(data)[1],]
i <- colnames(data_train) == "order_id"
j <- 1:length(i)
data_train <- data_train[,-j[i]]
dim(data_train)

## train a RandomForest model

f <- as.formula("as.factor(label) ~ .")
fit <- randomForest(f, data_train, ntree=2)

## test the model on the holdout test set

print(fit$importance)
print(fit)

predicted <- predict(fit, data)
data$predict <- predicted
confuse <- table(pred = predicted, true = data[,1])
print(confuse)

## export predicted labels to TSV

write.table(data, file=paste(dat_folder, "sample.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)

## export RF model to PMML

saveXML(pmml(fit), file=paste(dat_folder, "sample.rf.xml", sep="/"))
