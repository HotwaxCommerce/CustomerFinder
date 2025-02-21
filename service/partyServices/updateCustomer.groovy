import java.sql.Timestamp
import java.time.ZonedDateTime
import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityCondition
import org.moqui.entity.EntityFind

ZonedDateTime now = ZonedDateTime.now()
long nowMillis = now.toInstant().toEpochMilli()
Timestamp nowTimestamp = new Timestamp(nowMillis)



ExecutionContext ec = context.ec

Map<Object,Object> servicecall=ec.service.sync().name("partyServices.find#Customer").parameters(context).call()
partyIdL=servicecall.get("partyIdList")

for(customerIdV in partyIdL){

    EntityFind ef1 = ec.entity.find("customerFinder.contact.PartyContactMech").distinct(true)
    ef1.condition("partyId", EntityCondition.EQUALS, customerIdV)

    contactMechIdList = []
    EntityList el = ef1.list()

    for (EntityValue ev in (el as List<EntityValue>)) {
        contactMechIdList.add(ev.contactMechId)
    }

    for (id in contactMechIdList) {
        EntityFind ef2 = ec.entity.find("customerFinder.contact.PartyContactMech").distinct(true)
        OrderedList = [
                ec.entity.conditionFactory.makeCondition("contactMechId", EntityCondition.EQUALS, id),
                ec.entity.conditionFactory.makeCondition("partyId", EntityCondition.EQUALS, customerIdV)]
        ef2.condition(
                ec.entity.conditionFactory.makeCondition(OrderedList, EntityCondition.AND)
        );

        EntityValue ev2 = ef2.one()
        if (ev2.thruDate) continue
        if (ev2.contactMechPurposeId == "PostalPrimary") {

            if (toName || attnName || address1 || address2 || directions || city || postalCode) {


                ev2.set("thruDate", nowTimestamp.toString())
                ev2.update()
                def contactMech = ec.entity.makeValue("customerFinder.contact.ContactMech")
                fields = [
                        "contactMechTypeEnumId": "PostalAddress"
                ]
                contactMech.setAll(fields)
                contactMech.setSequencedIdPrimary()
                contactMech = contactMech.create()

                def partyContactMech = ec.entity.makeValue("customerFinder.contact.PartyContactMech")
                fields = [
                        "partyId"             : customerIdV,
                        "contactMechId"       : contactMech.contactMechId,
                        "contactMechPurposeId": "PostalPrimary",
                        "fromDate"            : nowTimestamp.toString()
                ]
                partyContactMech.setAll(fields)
                partyContactMech = partyContactMech.create()

                def postalAddressEntity = ec.entity.makeValue("customerFinder.contact.PostalAddress")
                fields = [
                        "contactMechId": contactMech.contactMechId,
                        "toName"       : context.toName,
                        "attnName"     : context.attnName,
                        "address1"     : context.address1,
                        "address2"     : context.address2,
                        "directions"   : context.directions,
                        "city"         : context.city,
                        "postalCode"   : context.postalCode
                ]
                postalAddressEntity.setAll(fields)
                postalAddressEntity = postalAddressEntity.create()
            }
//            context.postalId = id
        }
        if (ev2.contactMechPurposeId == "PhonePrimary") {

//            context.message="aagya"
            if (coutryCode || areaCode || mobileNumber) {

                ev2.set("thruDate", nowTimestamp.toString())
                ev2.update()

                def contactMech = ec.entity.makeValue("customerFinder.contact.ContactMech")
                fields = [
                        "contactMechTypeEnumId": "TelecomNumber"
                ]
                contactMech.setAll(fields)
                contactMech.setSequencedIdPrimary()
                contactMech = contactMech.create()

                def partyContactMech = ec.entity.makeValue("customerFinder.contact.PartyContactMech")
                fields = [
                        "partyId"             : customerIdV,
                        "contactMechId"       : contactMech.contactMechId,
                        "contactMechPurposeId": "PhonePrimary",
                        "fromDate"            : nowTimestamp.toString()
                ]
                partyContactMech.setAll(fields)
                partyContactMech = partyContactMech.create()

                def TelecomNumber = ec.entity.makeValue("customerFinder.contact.TelecomNumber")
                fields = [
                        "contactMechId": contactMech.contactMechId,
                        "countryCode"  : context.countryCode,
                        "contactNumber": context.mobileNumber
                ]
                TelecomNumber.setAll(fields)
                TelecomNumber = TelecomNumber.create()
            }

        }


    }

    context.partyId = customerIdV
}