## uncomment the following two lines to install the required libraries
#install.packages("pmml")
#install.packages("randomForest")

library(pmml)
library(randomForest)

## load the "baseline" reference data

dat_folder = './data'
data <- read.table(file=paste(dat_folder, "sample.tsv", sep="/"), sep="\t", quote="", na.strings="NULL", header=TRUE, encoding="UTF8")
colnames(data)[1] <- "is_fraud"

dim(data)
head(data)

## split data into test and train sets

set.seed(71)
split_ratio = 2/10
split = round(dim(data)[1] * split_ratio)

data_tests = data[1:split,]
dim(data_tests)
print(table(data_tests[,"is_fraud"]))

data_train = data[(split + 1):dim(data)[1],]
dim(data_train)

d_pos <- data_train[data_train[,"is_fraud"]==1,]
dim(d_pos)

d_neg <- data_train[data_train[,"is_fraud"]==0,]
dim(d_neg)

## train a RandomForest model

f <- as.formula("as.factor(is_fraud) ~ .")

data0 <- rbind(d_pos, d_neg[sample(1:nrow(d_neg), nrow(d_pos)),])
dim(data0)

fit <- randomForest(f, data0, ntree=2)

## test the model on the holdout test set

pred <- predict(fit, data_tests[,-1])
pred.tab <- table(pred = pred, true = data_tests[,1])

print(fit$importance)
print(fit)
print(pred.tab)

## export to PMML

pmml(fit)
saveXML(pmml(fit), file=paste(dat_folder, "sample.xml", sep="/"))
