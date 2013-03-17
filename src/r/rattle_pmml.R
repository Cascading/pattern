## uncomment the following lines to install required libraries
#install.packages("pmml")
#install.packages("randomForest")
#install.packages("rpart.plot")
#install.packages("nnet")
#install.packages("kernlab")
#install.packages("arules")
#install.packages("arulesViz")

require(graphics)
library(pmml)
library(randomForest)
library(rpart)
library(rpart.plot)
library(nnet)
library(XML)
library(kernlab)
library(arules)
library(arulesViz)

dat_folder <- './data'
COPY <- "Copyright (c)2012, Concurrent, Inc. (www.concurrentinc.com)"

## split data into test and train sets

data(iris)
iris_full <- iris
colnames(iris_full) <- c("sepal_length", "sepal_width", "petal_length", "petal_width", "species")

idx <- sample(150, 100)
iris_train <- iris_full[idx,]
iris_test <- iris_full[-idx,]


## train a Random Forest model
## example: http://mkseo.pe.kr/stats/?p=220
print("model: Random Forest")

f <- as.formula("as.factor(species) ~ .")
fit <- randomForest(f, data=iris_train, proximity=TRUE, ntree=50)

print(fit$importance)
print(fit)
print(table(iris_test$species, predict(fit, iris_test, type="class")))

plot(fit, log="y", main="Random Forest")
varImpPlot(fit)
MDSplot(fit, iris_full$species)

out <- iris_full
out$predict <- predict(fit, out, type="class")

write.table(out, file=paste(dat_folder, "iris.rf.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, copyright=COPY), file=paste(dat_folder, "iris.rf.xml", sep="/"))


## train a Linear Regression predictive model
## example: http://www2.warwick.ac.uk/fac/sci/moac/people/students/peter_cock/r/iris_lm/
print("model: Linear Regression - predictive model")

f <- as.formula("sepal_length ~ .")
fit <- lm(f, data=iris_train)

print(summary(fit))
print(table(round(iris_test$sepal_length), round(predict(fit, iris_test))))

op <- par(mfrow = c(3, 2))
plot(predict(fit), main="Linear Regression")
plot(iris_full$petal_length, iris_full$petal_width, pch=21, bg=c("red", "green3", "blue")[unclass(iris_full$species)], main="Edgar Anderson's Iris Data", xlab="petal length", ylab="petal width")
plot(fit)
par(op)

out <- iris_full
out$predict <- predict(fit, out)

write.table(out, file=paste(dat_folder, "iris.lm_p.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, copyright=COPY), file=paste(dat_folder, "iris.lm_p.xml", sep="/"))


## train a Recursive Partition classification tree
## example: http://www.r-bloggers.com/example-9-10-more-regression-trees-and-recursive-partitioning-with-partykit/
print("model: Recursive Partition")

f <- as.formula("species ~ .")
fit <- rpart(f, data=iris_train)

print(fit)
print(summary(fit))
print(table(iris_test$species, predict(fit, iris_test, type="class")))

op <- par(mfrow = c(2, 1))
prp(fit, extra=1, uniform=F, branch=1, yesno=F, border.col=0, xsep="/", main="Recursive Partition")
par(op)

out <- iris_full
out$predict <- predict(fit, out, type="class")

write.table(out, file=paste(dat_folder, "iris.rpart.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, copyright=COPY), file=paste(dat_folder, "iris.rpart.xml", sep="/"))


## train a single hidden-layer Neural Network
## example: http://statisticsr.blogspot.com/2008/10/notes-for-nnet.html
print("model: Neural Network")

samp <- c(sample(1:50,25), sample(51:100,25), sample(101:150,25))

ird <- data.frame(rbind(iris3[,,1], iris3[,,2], iris3[,,3]),
                  species = factor(c(rep("setosa",50), rep("versicolor", 50), rep("virginica", 50))))

f <- as.formula("species ~ .")
fit <- nnet(f, data=ird, subset=samp, size=2, rang=0.1, decay=5e-4, maxit=200)

print(fit)
print(summary(fit))
print(table(ird$species[-samp], predict(fit, ird[-samp,], type = "class")))

out <- ird
out$predict <- predict(fit, ird, type="class")

write.table(out, file=paste(dat_folder, "iris.nn.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, copyright=COPY), file=paste(dat_folder, "iris.nn.xml", sep="/"))


## train a Multinomial Regression model
## example: http://www.jameskeirstead.ca/r/how-to-multinomial-regression-models-in-r/
print("model: Multinomial Regression")

f <- as.formula("species ~ .")
fit <- multinom(f, data=iris_train)

print(summary(fit))
print(table(iris_test$species, predict(fit, iris_test)))

out <- iris_full
out$predict <- predict(fit, out, type="class")

write.table(out, file=paste(dat_folder, "iris.multinom.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, dataset=iris_train, copyright=COPY), file=paste(dat_folder, "iris.multinom.xml", sep="/"))


## train a K-Means clustering model
## example: http://mkseo.pe.kr/stats/?p=15
print("model: K-Means Clustering")

ds <- iris_full[,-5]
fit <- kmeans(ds, 3)

print(fit)
print(summary(fit))
print(table(fit$cluster, iris_full$species))

op <- par(mfrow = c(1, 1))
plot(iris_full$sepal_length, iris_full$sepal_width, pch = 23, bg = c("blue", "red", "green")[fit$cluster], main="K-Means Clustering")
points(fit$centers[,c(1, 2)], col=1:3, pch=8, cex=2)
par(op)

out <- iris_full
out$predict <- fit$cluster

write.table(out, file=paste(dat_folder, "iris.kmeans.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, copyright=COPY), file=paste(dat_folder, "iris.kmeans.xml", sep="/"))


## train a Hierarchical Clustering model
## example: http://mkseo.pe.kr/stats/?p=15
print("model: Hierarchical Clustering")

i = as.matrix(iris_full[,-5])
fit <- hclust(dist(i), method = "average")

initial <- tapply(i, list(rep(cutree(fit, 3), ncol(i)), col(i)), mean)
dimnames(initial) <- list(NULL, dimnames(i)[[2]])
kls = cutree(fit, 3)

print(fit)
print(table(iris_full$species, kls))

op <- par(mfrow = c(1, 1))
plclust(fit, main="Hierarchical Clustering")
par(op)

out <- iris_full
out$predict <- kls

write.table(out, file=paste(dat_folder, "iris.hc.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, data=iris, centers=initial, copyright=COPY), file=paste(dat_folder, "iris.hc.xml", sep="/"))


## train a General Linear Regression model (in this case, Logistic Regression)
## example: http://www.stat.cmu.edu/~cshalizi/490/clustering/clustering01.r
print("model: Logistic Regression")

myiris <- cbind(iris_full, setosa=ifelse(iris_full$species=="setosa", 1, 0))
myiris <- cbind(myiris, versicolor=ifelse(iris_full$species=="versicolor", 1, 0))
myiris <- cbind(myiris, virginica=ifelse(iris_full$species=="virginica", 1, 0))
myiris <- myiris[,-5] # drop the old labels

f <- as.formula("setosa ~ sepal_length + sepal_width + petal_length + petal_width")
fit <- glm(f, family=binomial, data=myiris)

print(summary(fit))
print(table(cbind(round(fitted(fit)), myiris$setosa)))

op <- par(mfrow = c(3, 2))
cols=c(1, 2)
plot(iris_full[,cols], type="n")
points(iris_full[iris_full$species=="setosa", cols], col=1, pch="*")
points(iris_full[iris_full$species=="versicolor", cols], col=2, pch="*")
points(iris_full[iris_full$species=="virginica", cols], col=3, pch="*")

cols=c(3, 4)
plot(iris_full[,cols], type="n")
points(iris_full[iris_full$species=="setosa", cols], col=1, pch="*")
points(iris_full[iris_full$species=="versicolor", cols], col=2, pch="*")
points(iris_full[iris_full$species=="virginica", cols], col=3, pch="*")

plot(fit)
par(op)

out <- iris_full
out$predict <- round(fitted(fit))

write.table(out, file=paste(dat_folder, "iris.glm.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, copyright=COPY), file=paste(dat_folder, "iris.glm.xml", sep="/"))


## train a Support Vector Machine model
## example: https://support.zementis.com/entries/21176632-what-types-of-svm-models-built-in-r-can-i-export-to-pmml
print("model: Support Vector Machine")

f <- as.formula("species ~ .")
fit <- ksvm(f, data=iris_train, kernel="rbfdot", prob.model=TRUE)

print(fit)
print(table(iris_test$species, predict(fit, iris_test)))

out <- iris_full
out$predict <- predict(fit, out)

write.table(out, file=paste(dat_folder, "iris.svm.tsv", sep="/"), quote=FALSE, sep="\t", row.names=FALSE)
saveXML(pmml(fit, dataset=iris_train, copyright=COPY), file=paste(dat_folder, "iris.svm.xml", sep="/"))


## train an Association Rules model
## example: http://jmlr.csail.mit.edu/papers/volume12/hahsler11a/hahsler11a.pdf
print("model: Association Rules")

data("Groceries")
rules <- apriori(Groceries, parameter = list(supp = 0.001, conf = 0.8))

print(rules)
print(summary(rules))
rules_high_lift <- head(sort(rules, by="lift"), 4)
print(inspect(rules_high_lift))

#plot(rules, control=list(jitter=2))
plot(rules_high_lift, method="graph", control=list(type="items"))
itemFrequencyPlot(Groceries, support = 0.05, cex.names=0.8)

rules_high_lift <- head(sort(rules, by="confidence"), 10)

#WRITE(Groceries, file=paste(dat_folder, "groc.arules.csv", sep="/"), sep=",")
saveXML(pmml(rules_high_lift, copyright=COPY), file=paste(dat_folder, "groc.arules.xml", sep="/"))


## TODO:
## pmml.rsf
