## uncomment the following lines to install required libraries
#install.packages("pmml")
#install.packages("randomForest")
#install.packages("rpart.plot")
#install.packages("nnet")

require(graphics)
library(pmml)
library(randomForest)
library(rpart)
library(rpart.plot)
library(nnet)

dat_folder <- './data'

## split data into test and train sets

data(iris)
idx <- sample(150, 100)
iris_train <- iris[idx,]
iris_test <- iris[-idx,]

## train a RandomForest model

f <- as.formula("as.factor(Species) ~ .")
fit <- randomForest(f, data=iris, proximity=TRUE, ntree=100)

print(fit$importance)
print(fit)
plot(fit, log="y")
MDSplot(fit, iris$Species)

saveXML(pmml(fit), file=paste(dat_folder, "iris.rf.xml", sep="/"))

## train an "rpart" recursive partition classification tree

f <- as.formula("Species ~ .")
fit <- rpart(f, data=iris_train)

print(fit)
print(table(iris_test$Species, predict(fit, iris_test, type="class")))
prp(fit, extra=1, uniform=F, branch=1, yesno=F, border.col=0, xsep="/")

saveXML(pmml(fit), file=paste(dat_folder, "iris.rpart.xml", sep="/"))

## train a single hidden-layer neural network

samp <- c(sample(1:50,25), sample(51:100,25), sample(101:150,25))

ird <- data.frame(rbind(iris3[,,1], iris3[,,2], iris3[,,3]),
                  species = factor(c(rep("setosa",50), rep("versicolor", 50), rep("virginica", 50))))

f <- as.formula("species ~ .")
fit <- nnet(f, data=ird, subset=samp, size=2, rang=0.1, decay=5e-4, maxit=200)

print(fit)
print(summary(fit))
print(table(ird$species[-samp], predict(fit, ird[-samp,], type = "class")))

saveXML(pmml(fit), file=paste(dat_folder, "iris.nn.xml", sep="/"))

## train a linear regression model

f <- as.formula("Sepal.Length ~ .")
fit <- lm(f, data=iris_train)

print(summary(fit))
plot(predict(fit))
plot(fit)

plot(iris$Petal.Length, iris$Petal.Width, pch=21, bg=c("red","green3","blue")[unclass(iris$Species)], main="Edgar Anderson's Iris Data", xlab="Petal length", ylab="Petal width")

saveXML(pmml(fit), file=paste(dat_folder, "iris.lm.xml", sep="/"))

## train a general linear regression model

td <- cbind(iris_train, data.frame(isSetosa=(iris_train$Species == 'setosa')))
head(td)

f <- isSetosa ~ Sepal.Length + Sepal.Width + Petal.Length + Petal.Width
fit <- glm(f, data=td, family="binomial")

prob <- predict(fit, newdata=iris_test, type='response')
round(prob, 3)

print(summary(fit))
plot(fit)

saveXML(pmml(fit), file=paste(dat_folder, "iris.glm.xml", sep="/"))

## train a support vector machine

f <- as.formula("Species ~ .")
fit <- ksvm(f, data=iris_train, type="C-bsvc", kernel=rbfdot(sigma=0.1), C=10, prob.model=TRUE)

print(fit)
print(table(iris_test$Species, predict(fit, iris_test)))

saveXML(pmml(fit, dataset=iris_train), file=paste(dat_folder, "iris.svm.xml", sep="/"))

## train a k-means clustering model

ds <- iris[,-5]
fit <- kmeans(ds, 3)

plot(iris$Sepal.Length, iris$Sepal.Width, pch = 23, bg = c("blue", "red", "green")[fit$cluster])
points(fit$centers[,c(1,2)], col=1:3, pch=8, cex=2)
table(fit$cluster, iris$Species)

saveXML(pmml(fit), file=paste(dat_folder, "iris.kmeans.xml", sep="/"))

## train a hierarchical clustering model (DOES NOT WORK!)

fit <- hclust(dist(iris_train[,1:4]))
plot(fit)

saveXML(pmml(fit, data=iris_train), file=paste(dat_folder, "iris.hc.xml", sep="/"))
